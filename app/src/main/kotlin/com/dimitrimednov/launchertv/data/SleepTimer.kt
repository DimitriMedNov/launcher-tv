package com.dimitrimednov.launchertv.data

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Temporizador para dormir. Una app normal no puede apagar la tele; lo hace [SleepService],
 * un servicio de accesibilidad que pide a Android "bloquear la pantalla", lo que en la tele
 * la manda a reposo. Se activa una vez por adb (ver README).
 */
object SleepTimer {
    private const val PREFS = "cuarto"
    private const val KEY_END = "timer_end"
    private const val KEY_MINUTES = "timer_minutes"

    /** Hora (System.currentTimeMillis) en que se apaga, y minutos elegidos; null si está apagado. */
    data class Running(val endAt: Long, val minutes: Int)

    private val _running = MutableStateFlow<Running?>(null)
    val running: StateFlow<Running?> = _running.asStateFlow()

    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val end = prefs.getLong(KEY_END, 0L)
        _running.value = if (end > System.currentTimeMillis()) Running(end, prefs.getInt(KEY_MINUTES, 0)) else null
    }

    /** Devuelve false si el servicio no está activo (la cuenta se ve, pero la tele no se apagaría). */
    fun set(context: Context, minutes: Int): Boolean {
        val running = if (minutes > 0) Running(System.currentTimeMillis() + minutes * 60_000L, minutes) else null
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
            putLong(KEY_END, running?.endAt ?: 0L)
            putInt(KEY_MINUTES, minutes)
        }
        _running.value = running
        SleepService.instance?.schedule()
        return minutes == 0 || SleepService.instance != null
    }

    internal fun clear(context: Context) = set(context, 0)
}

class SleepService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private val sleep = Runnable {
        SleepTimer.clear(this)
        performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
    }

    override fun onServiceConnected() {
        instance = this
        SleepTimer.load(this)
        schedule()
    }

    fun schedule() {
        handler.removeCallbacks(sleep)
        val running = SleepTimer.running.value ?: return
        val wait = (running.endAt - System.currentTimeMillis()).coerceAtLeast(0L)
        handler.postAtTime(sleep, SystemClock.uptimeMillis() + wait)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        handler.removeCallbacks(sleep)
        if (instance === this) instance = null
        super.onDestroy()
    }

    companion object {
        @Volatile
        var instance: SleepService? = null
            private set
    }
}
