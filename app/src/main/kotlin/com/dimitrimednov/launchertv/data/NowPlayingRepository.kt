package com.dimitrimednov.launchertv.data

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.service.notification.NotificationListenerService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Servicio vacío: solo existe para que Android deje leer las sesiones de medios.
 * Se activa una vez por adb:
 * `adb shell cmd notification allow_listener com.dimitrimednov.launchertv/.data.MediaListenerService`
 */
class MediaListenerService : NotificationListenerService()

/** Lo último que reproduce una app (Spotify, Netflix, Disney+…), leído de su sesión de medios. */
data class NowPlaying(
    val packageName: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val positionMs: Long,
    val durationMs: Long,
    val isPlaying: Boolean,
) {
    val progress: Float get() = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
}

class NowPlayingRepository(private val context: Context) {
    private val manager = context.getSystemService(MediaSessionManager::class.java)
    private val listener = ComponentName(context, MediaListenerService::class.java)
    private var controllers: Map<String, MediaController> = emptyMap()

    /** Emite un mapa paquete → lo que reproduce. Vacío si falta el permiso de notificaciones. */
    fun sessions(): Flow<Map<String, NowPlaying>> = callbackFlow {
        val handler = Handler(Looper.getMainLooper())
        val watched = mutableMapOf<MediaController, MediaController.Callback>()

        fun publish() {
            controllers = watched.keys.associateBy { it.packageName }
            trySend(watched.keys.mapNotNull { it.toNowPlaying() }.associateBy { it.packageName })
        }

        fun watch(list: List<MediaController>?) {
            watched.forEach { (controller, callback) -> controller.unregisterCallback(callback) }
            watched.clear()
            list.orEmpty().forEach { controller ->
                val callback = object : MediaController.Callback() {
                    override fun onMetadataChanged(metadata: MediaMetadata?) = publish()
                    override fun onPlaybackStateChanged(state: PlaybackState?) = publish()
                    override fun onSessionDestroyed() = publish()
                }
                controller.registerCallback(callback, handler)
                watched[controller] = callback
            }
            publish()
        }

        val sessionsListener = MediaSessionManager.OnActiveSessionsChangedListener { watch(it) }
        val allowed = try {
            watch(manager?.getActiveSessions(listener))
            manager?.addOnActiveSessionsChangedListener(sessionsListener, listener, handler)
            true
        } catch (_: SecurityException) {
            trySend(emptyMap())
            false
        }
        awaitClose {
            if (allowed) manager?.removeOnActiveSessionsChangedListener(sessionsListener)
            watched.forEach { (controller, callback) -> controller.unregisterCallback(callback) }
        }
    }

    fun togglePlayback(packageName: String) {
        val controller = controllers[packageName] ?: return
        if (controller.playbackState?.state == PlaybackState.STATE_PLAYING) {
            controller.transportControls.pause()
        } else {
            controller.transportControls.play()
        }
    }

    private fun MediaController.toNowPlaying(): NowPlaying? {
        val meta = metadata ?: return null
        val title = meta.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: meta.description.title?.toString()
            ?: return null
        val state = playbackState
        val position = state?.let {
            if (it.state == PlaybackState.STATE_PLAYING && it.lastPositionUpdateTime > 0) {
                it.position + ((SystemClock.elapsedRealtime() - it.lastPositionUpdateTime) * it.playbackSpeed).toLong()
            } else {
                it.position
            }
        } ?: 0L
        return NowPlaying(
            packageName = packageName,
            title = title,
            artist = meta.getString(MediaMetadata.METADATA_KEY_ARTIST)?.takeIf { it.isNotBlank() },
            album = meta.getString(MediaMetadata.METADATA_KEY_ALBUM)?.takeIf { it.isNotBlank() },
            positionMs = position.coerceAtLeast(0),
            durationMs = meta.getLong(MediaMetadata.METADATA_KEY_DURATION),
            isPlaying = state?.state == PlaybackState.STATE_PLAYING,
        )
    }
}
