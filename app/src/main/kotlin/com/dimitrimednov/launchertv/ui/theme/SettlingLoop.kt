package com.dimitrimednov.launchertv.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay

/**
 * Animación que se repite [cycles] vueltas y luego se queda quieta en [rest]. Sustituye a las
 * animaciones infinitas: mientras algo se mueve, la tele repinta la pantalla entera en cada cuadro
 * (con el video de fondo, eso la frena). Vuelve a empezar cuando cambia [key].
 * Con [reverse] cada vuelta va de [from] a [to] y regresa; si no, salta a [from] y avanza a [to].
 */
@Composable
fun rememberSettlingLoop(
    periodMs: Int,
    from: Float,
    to: Float,
    rest: Float,
    key: Any? = Unit,
    cycles: Int = 3,
    reverse: Boolean = true,
    delayMs: Int = 0,
    easing: Easing = FastOutSlowInEasing,
): State<Float> {
    val value = remember { Animatable(from) }
    LaunchedEffect(key) {
        delay(delayMs.toLong())
        repeat(cycles) {
            if (!reverse) value.snapTo(from)
            value.animateTo(to, tween(periodMs, easing = easing))
            if (reverse) value.animateTo(from, tween(periodMs, easing = easing))
        }
        if (!reverse) value.snapTo(from)
        value.animateTo(rest, tween(periodMs / 2, easing = easing))
    }
    return value.asState()
}
