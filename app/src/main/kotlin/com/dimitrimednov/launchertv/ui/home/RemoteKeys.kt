package com.dimitrimednov.launchertv.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import com.dimitrimednov.launchertv.ui.theme.Motion
import android.view.KeyEvent as AndroidKeyEvent

/**
 * Traduce las teclas del control (y del teclado, para probar en el emulador) al [Navigator].
 * OK corto = activar; OK mantenido 600 ms = menú de la app; Atrás nunca cierra el launcher.
 */
@Composable
fun Modifier.remoteKeys(navigator: Navigator, onAnyKey: () -> Unit): Modifier {
    val ok = remember { OkPress() }
    return onPreviewKeyEvent { event ->
        val down = event.type == KeyEventType.KeyDown
        val up = event.type == KeyEventType.KeyUp
        val native = event.nativeKeyEvent
        if (down) onAnyKey()
        when (event.key) {
            Key.DirectionUp -> { if (down) navigator.onDirection(Dir.Up); true }
            Key.DirectionDown -> { if (down) navigator.onDirection(Dir.Down); true }
            Key.DirectionLeft -> { if (down) navigator.onDirection(Dir.Left); true }
            Key.DirectionRight -> { if (down) navigator.onDirection(Dir.Right); true }
            Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
                when {
                    down && native.repeatCount == 0 -> ok.start(native.eventTime)
                    down && !ok.longFired && (native.isLongPress || native.eventTime - ok.downAt >= Motion.LONG_PRESS_MS) -> {
                        ok.longFired = true
                        navigator.onLongOk()
                    }
                    up -> {
                        if (!ok.longFired && ok.downAt >= 0) navigator.onOk()
                        ok.reset()
                    }
                }
                true
            }
            Key.Back, Key.Escape -> { if (up) navigator.onBack(); true }
            else -> when (event.key.nativeKeyCode) {
                AndroidKeyEvent.KEYCODE_BUTTON_A -> { if (up) navigator.onOk(); true }
                else -> {
                    // Cualquier otra tecla solo cierra el protector; el resto lo maneja la tele.
                    if (down && navigator.state.saver) { navigator.closeSaver(); true } else false
                }
            }
        }
    }
}

private class OkPress {
    var downAt = -1L
    var longFired = false

    fun start(time: Long) {
        downAt = time
        longFired = false
    }

    fun reset() {
        downAt = -1L
        longFired = false
    }
}
