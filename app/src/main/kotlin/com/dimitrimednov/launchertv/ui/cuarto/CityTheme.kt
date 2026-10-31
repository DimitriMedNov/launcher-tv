package com.dimitrimednov.launchertv.ui.cuarto

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.dimitrimednov.launchertv.R

/** Colores del diseño de la tele del cuarto (`Launcher Cuarto v6.dc.html`, constantes DAY y NIGHT). */
@Immutable
data class CityColors(
    val bg: Color,
    val sf: Color,
    val sf2: Color,
    val ln: Color,
    val lnS: Color,
    val tx: Color,
    val mu: Color,
    val mu2: Color,
    val ac: Color,
    val ac2: Color,
    val onAc: Color,
    val glow: Color,
    val err: Color,
    val svbg: Color,
    val svtx: Color,
    /** Luna del inicio y del protector. */
    val moon: Color,
    val bat: Color,
    val night: Boolean,
)

val DayColors = CityColors(
    bg = Color(0xFF07080A), sf = Color(0xFF111317), sf2 = Color(0xFF1A1D23),
    ln = Color.White.copy(alpha = .08f), lnS = Color.White.copy(alpha = .16f),
    tx = Color(0xFFEDEDE8), mu = Color(0xFF9A9EA6), mu2 = Color(0xFFC4C7CC),
    ac = Color(0xFFFFD23F), ac2 = Color(0xFF7FA3C4), onAc = Color(0xFF0A0A0A),
    glow = Color(0xFFFFD23F).copy(alpha = .35f), err = Color(0xFFFF6B5E),
    svbg = Color(0xFF030304), svtx = Color(0xFFE2E2DC),
    moon = Color(0xFFE8E3D3), bat = Color(0xFFC4C7CC), night = false,
)

private val Sand = Color(0xFFFFC896)

val NightColors = CityColors(
    bg = Color(0xFF0A0705), sf = Color(0xFF140F0B), sf2 = Color(0xFF1D1610),
    ln = Sand.copy(alpha = .08f), lnS = Sand.copy(alpha = .16f),
    tx = Color(0xFFE8D8C2), mu = Color(0xFFAC9A82), mu2 = Color(0xFFCBB9A1),
    ac = Color(0xFFE8A33D), ac2 = Color(0xFFB98466), onAc = Color(0xFF140B02),
    glow = Color(0xFFE8A33D).copy(alpha = .28f), err = Color(0xFFF08A70),
    svbg = Color(0xFF050302), svtx = Color(0xFF9C8668),
    moon = Color(0xFFBFA982), bat = Color(0xFFB89C78), night = true,
)

val LocalCity = staticCompositionLocalOf { DayColors }

/** Colores actuales; el cambio entre día y noche es un fundido de 2 s, como en el diseño. */
val city: CityColors @Composable get() = LocalCity.current

@Composable
fun CityTheme(night: Boolean, content: @Composable () -> Unit) {
    val t = if (night) NightColors else DayColors
    @Composable fun c(target: Color, label: String) = animateColorAsState(target, tween(2000), label = label).value
    val colors = CityColors(
        bg = c(t.bg, "bg"), sf = c(t.sf, "sf"), sf2 = c(t.sf2, "sf2"), ln = c(t.ln, "ln"), lnS = c(t.lnS, "lnS"),
        tx = c(t.tx, "tx"), mu = c(t.mu, "mu"), mu2 = c(t.mu2, "mu2"), ac = c(t.ac, "ac"), ac2 = c(t.ac2, "ac2"),
        onAc = c(t.onAc, "onAc"), glow = c(t.glow, "glow"), err = c(t.err, "err"), svbg = c(t.svbg, "svbg"),
        svtx = c(t.svtx, "svtx"), moon = c(t.moon, "moon"), bat = c(t.bat, "bat"), night = night,
    )
    CompositionLocalProvider(LocalCity provides colors, content = content)
}

/** Color de marca de una app; de noche se mezcla con ámbar para no deslumbrar. */
fun CityColors.brand(color: Color, nightMix: Float = .5f): Color =
    if (night) lerp(color, Color(0xFFE8A33D), nightMix) else color

/** `cubic-bezier(0.22, 1, 0.36, 1)` del diseño. */
val CityEase = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

fun <T> cityTween(ms: Int, delay: Int = 0) = tween<T>(ms, delay, CityEase)

val BarlowCondensed = FontFamily(
    Font(R.font.barlow_condensed_medium, FontWeight.Medium),
    Font(R.font.barlow_condensed_semibold, FontWeight.SemiBold),
    Font(R.font.barlow_condensed_bold, FontWeight.Bold),
)

val Barlow = FontFamily(
    Font(R.font.barlow_regular, FontWeight.Normal),
    Font(R.font.barlow_medium, FontWeight.Medium),
    Font(R.font.barlow_semibold, FontWeight.SemiBold),
)
