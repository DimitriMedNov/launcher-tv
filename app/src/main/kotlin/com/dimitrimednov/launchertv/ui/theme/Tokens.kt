package com.dimitrimednov.launchertv.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dimitrimednov.launchertv.R

/** Colores del diseño (`Launcher TV v5.dc.html`). */
object Palette {
    val Bg = Color(0xFF0D0D0F)
    val Panel = Color(0xFF1C1C1E)
    val White = Color(0xFFFFFFFF)
    val Label = Color(0xFFE5E5EA)
    val Secondary = Color(0xFFC7C7CC)
    val Tertiary = Color(0xFFAEAEB2)
    val Muted = Color(0xFF8E8E93)
    val OnWhiteMuted = Color(0xFF48484A)
    val Accent = Color(0xFF0099FE)
    val Live = Color(0xFFFF453A)
    val Signal = Color(0xFF30D158)
    val NoSignal = Color(0xFF636366)
    val ToggleOff = Color(0xFF48484A)

    /** Blanco con transparencia, como los `rgba(255,255,255,a)` del diseño. */
    fun glass(alpha: Float) = White.copy(alpha = alpha)
}

/** Medidas fijas del lienzo de 1920×1080 (1 dp = 1 px del diseño, ver [DesignCanvas]). */
object Layout {
    val ContentLeft: Dp = 216.dp
    val ChromeTop: Dp = 44.dp
    val ChromeRight: Dp = 72.dp
    val SectionTop: Dp = 156.dp
    val GridWidth: Dp = 1608.dp
    const val GRID_COLUMNS = 6
    val GridGap: Dp = 28.dp
    val TileRadius: Dp = 32.dp
}

/**
 * Curvas y duraciones del diseño. `EASE` es `cubic-bezier(.2,.9,.25,1)`; los rebotes del
 * diseño (`cubic-bezier(.34,1.4,.5,1)`) se hacen con resortes de Compose.
 */
object Motion {
    val Ease = CubicBezierEasing(0.2f, 0.9f, 0.25f, 1f)
    val EaseIn = CubicBezierEasing(0.42f, 0f, 1f, 1f)

    fun <T> ease(ms: Int, delay: Int = 0) = tween<T>(durationMillis = ms, delayMillis = delay, easing = Ease)
    fun <T> bouncy() = spring<T>(dampingRatio = 0.62f, stiffness = Spring.StiffnessMediumLow)
    fun <T> settle() = spring<T>(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)

    const val PRESS_MS = 130
    const val LONG_PRESS_MS = 600L
    const val IDLE_SAVER_MS = 90_000L
    const val DOUBLE_BACK_MS = 2_000L
}

private fun manrope(weight: Int) = Font(
    resId = R.font.manrope,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Manrope = FontFamily(
    manrope(400),
    manrope(500),
    manrope(600),
    manrope(700),
    manrope(800),
)
