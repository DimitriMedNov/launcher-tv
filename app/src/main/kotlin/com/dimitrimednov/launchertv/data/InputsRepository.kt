package com.dimitrimednov.launchertv.data

import androidx.core.content.edit
import android.content.Context
import android.content.Intent
import android.media.tv.TvContract
import android.media.tv.TvInputInfo
import android.media.tv.TvInputManager
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

/** Aparato conectado a una entrada; define el ícono y el nombre por defecto. */
enum class InputKind(val label: String) {
    Console("Consola"),
    SetTopBox("Decodificador"),
    Laptop("Computadora"),
    Soundbar("Barra de sonido"),
    Player("Reproductor"),
    Hdmi("HDMI"),
    Antenna("Antena"),
}

data class TvInput(
    val id: String,
    /** Número de puerto HDMI (1 a 4), o null para la antena. */
    val port: Int?,
    val kind: InputKind,
    val name: String,
    val hasSignal: Boolean,
)

/**
 * Entradas HDMI y antena de la tele. Confirmado en la tele de la sala (30-oct-2026):
 * `getInputState` vale 0 con señal y 1 sin señal; `cable_connection_status` no sirve.
 */
class InputsRepository(private val context: Context) {
    private val manager = context.getSystemService(TvInputManager::class.java)
    private val prefs = context.getSharedPreferences("inputs", Context.MODE_PRIVATE)
    private val edits = MutableStateFlow(0)

    /** Emite la lista cada vez que una entrada gana o pierde señal. */
    fun inputs(): Flow<List<TvInput>> = callbackFlow {
        if (manager == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val callback = object : TvInputManager.TvInputCallback() {
            override fun onInputStateChanged(inputId: String, state: Int) { trySend(read()) }
            override fun onInputAdded(inputId: String) { trySend(read()) }
            override fun onInputRemoved(inputId: String) { trySend(read()) }
        }
        launch { edits.collect { trySend(read()) } }
        manager.registerCallback(callback, Handler(Looper.getMainLooper()))
        awaitClose { manager.unregisterCallback(callback) }
    }

    fun setKind(inputId: String, kind: InputKind) {
        prefs.edit { putString(inputId, kind.name) }
        edits.value++
    }

    /** Intent para cambiar a la entrada. La antena abre Live TV. */
    fun switchIntent(input: TvInput): Intent? =
        if (input.port == null) {
            context.packageManager.getLeanbackLaunchIntentForPackage(LIVE_TV_PACKAGE)
        } else {
            Intent(Intent.ACTION_VIEW, TvContract.buildChannelUriForPassthroughInput(input.id))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

    private fun read(): List<TvInput> {
        val all = manager?.tvInputList.orEmpty()
        val hdmi = all.filter { it.type == TvInputInfo.TYPE_HDMI && it.parentId == null }
            .map { info -> info to portOf(info) }
            .sortedBy { it.second }
            .map { (info, port) -> toInput(info, port, InputKind.Hdmi) }
        val antenna = all.firstOrNull { it.type == TvInputInfo.TYPE_TUNER && isHardware(it) }
            ?.let { toInput(it, null, InputKind.Antenna) }
        return hdmi + listOfNotNull(antenna)
    }

    private fun toInput(info: TvInputInfo, port: Int?, fallback: InputKind): TvInput {
        val kind = prefs.getString(info.id, null)?.let { saved -> InputKind.entries.firstOrNull { it.name == saved } }
            ?: fallback
        val name = when (kind) {
            InputKind.Hdmi -> "HDMI ${port ?: ""}".trim()
            else -> kind.label
        }
        val state = manager?.getInputState(info.id) ?: TvInputManager.INPUT_STATE_DISCONNECTED
        return TvInput(info.id, port, kind, name, state == TvInputManager.INPUT_STATE_CONNECTED)
    }

    /** Los ids de Hisense terminan en `/HW2`…`/HW5` para HDMI 1…4. */
    private fun portOf(info: TvInputInfo): Int {
        val label = info.loadLabel(context).toString()
        return Regex("""\d+""").find(label)?.value?.toIntOrNull()
            ?: info.id.substringAfterLast("/HW", "").toIntOrNull()?.minus(1)
            ?: Int.MAX_VALUE
    }

    /** Los sintonizadores de hardware de Hisense viven en `com.vt.source.tuner`; los de apps (Google TV) no cuentan. */
    private fun isHardware(info: TvInputInfo) = info.id.contains("/HW")
}
