package com.dimitrimednov.launchertv.ui.home

import com.dimitrimednov.launchertv.data.BackgroundItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Qué fondo se ve ahora. Una foto se queda [PHOTO_MS] en el inicio y [SAVER_PHOTO_MS] en el
 * protector; un video, hasta que termina (la pantalla avisa con [videoEnded]).
 * En el protector siempre empieza un video y luego alterna foto, video, foto…; ahí el video se
 * repite hasta cumplir [SAVER_PHOTO_MS] y entonces termina la vuelta en curso (nunca se corta).
 * Sin fondos propios se queda el degradado de anochecer.
 */
class BackgroundRotation(private val scope: CoroutineScope) {
    private var items: List<BackgroundItem> = emptyList()
    private var index = 0
    private var photoTimer: Job? = null
    private var videoTimer: Job? = null
    private var saver = false

    private val _current = MutableStateFlow(BackgroundItem.Builtin)
    val current: StateFlow<BackgroundItem> = _current.asStateFlow()

    /** Solo un fondo propio: el video se repite en bucle en vez de cambiar. */
    val single: Boolean get() = items.size <= 1

    private val _loopVideo = MutableStateFlow(false)
    /** El video actual debe repetirse (protector, mientras no cumpla su tiempo). */
    val loopVideo: StateFlow<Boolean> = _loopVideo.asStateFlow()

    /** Cambia la lista solo si cambiaron los archivos, para no saltar de fondo al volver al inicio. */
    fun setItems(newItems: List<BackgroundItem>) {
        if (newItems.map { it.id }.sorted() == items.map { it.id }.sorted()) return
        items = newItems
        // La lista viene revuelta: cada arranque empieza con una foto o un video al azar.
        index = 0
        show(items.firstOrNull() ?: BackgroundItem.Builtin)
    }

    /**
     * Al abrir el protector, si se ve una foto salta al siguiente video. Al cerrarlo, la foto
     * que se ve toma la duración del inicio.
     */
    fun setSaver(on: Boolean) {
        if (on == saver) return
        saver = on
        if (_current.value.isVideo) {
            if (on) startVideoTimer() else stopVideoTimer()
            return
        }
        if (!(on && advanceTo { it.isVideo })) startPhotoTimer()
    }

    fun videoEnded(item: BackgroundItem) {
        if (item.id == _current.value.id) next()
    }

    private fun next() {
        if (single) return
        val wasVideo = _current.value.isVideo
        // En el protector alterna video y foto; si falta uno de los dos tipos, sigue con el que haya.
        if (saver && advanceTo { it.isVideo != wasVideo }) return
        advanceTo { true }
    }

    /** Pasa al siguiente fondo de la lista que cumpla [match]. Devuelve false si no hay ninguno. */
    private fun advanceTo(match: (BackgroundItem) -> Boolean): Boolean {
        val offset = (1..items.size).firstOrNull { match(items[(index + it) % items.size]) } ?: return false
        index = (index + offset) % items.size
        show(items[index])
        return true
    }

    private fun show(item: BackgroundItem) {
        _current.value = item
        photoTimer?.cancel()
        stopVideoTimer()
        if (!item.isVideo) startPhotoTimer() else if (saver) startVideoTimer()
    }

    private fun startVideoTimer() {
        videoTimer?.cancel()
        _loopVideo.value = true
        videoTimer = scope.launch { delay(SAVER_PHOTO_MS); _loopVideo.value = false }
    }

    private fun stopVideoTimer() {
        videoTimer?.cancel()
        _loopVideo.value = false
    }

    private fun startPhotoTimer() {
        photoTimer?.cancel()
        photoTimer = scope.launch { delay(if (saver) SAVER_PHOTO_MS else PHOTO_MS); next() }
    }

    private companion object {
        const val PHOTO_MS = 5 * 60_000L
        const val SAVER_PHOTO_MS = 90_000L
    }
}
