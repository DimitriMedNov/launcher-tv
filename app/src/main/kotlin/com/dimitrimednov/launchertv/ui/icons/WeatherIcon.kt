package com.dimitrimednov.launchertv.ui.icons

import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import com.dimitrimednov.launchertv.data.WeatherKind
import com.dimitrimednov.launchertv.ui.theme.rememberSettlingLoop
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val CloudPath: Path = PathParser()
    .parsePathString("M12 30 H38 A9 9 0 0 0 38 12 A13 13 0 0 0 13.5 8.5 A11 11 0 0 0 12 30 Z").toPath()

private val Sun = listOf(Color(0xFFFFD60A), Color(0xFFFF9F0A))
private val HeatSun = listOf(Color(0xFFFF9F0A), Color(0xFFFF453A))
private val WhiteCloud = listOf(Color(0xFFFFFFFF), Color(0xFFE5E5EA))
private val LightCloud = listOf(Color(0xFFF2F2F7), Color(0xFFD1D1D6))
private val GrayCloud = listOf(Color(0xFFAEAEB2), Color(0xFF8E8E93))
private val DarkCloud = listOf(Color(0xFF636366), Color(0xFF48484A))
private val Moon = listOf(Color(0xFFF2F7FF), Color(0xFFB5C8E8))
private val Drop = Color(0xFF0A84FF)
private val Bolt = Color(0xFFFFD60A)
private val Fog = Color(0xFFAEAEB2)

/** Ícono del clima a color (64×64 como en `icons.js`), con su animación suave al aparecer o cambiar el clima. */
@Composable
fun WeatherIcon(kind: WeatherKind, size: Dp, animate: Boolean = true) {
    val sway by rememberSettlingLoop(4000, -7f, 7f, rest = 0f, key = kind, cycles = 2)
    val drift by rememberSettlingLoop(3000, -1.5f, 1.5f, rest = 0f, key = kind, cycles = 2)
    val fall by rememberSettlingLoop(1600, 0f, 1f, rest = .3f, key = kind, cycles = 6, reverse = false, easing = LinearEasing)
    val cycle by rememberSettlingLoop(6000, 0f, 1f, rest = .5f, key = kind, cycles = 2, reverse = false, easing = LinearEasing)
    val a = if (animate) Anim(sway, drift, fall, cycle) else Anim(0f, 0f, 0.3f, 0.5f)

    Canvas(Modifier.size(size)) {
        scale(this.size.width / 64f, pivot = Offset.Zero) { drawWeather(kind, a) }
    }
}

private data class Anim(val sway: Float, val drift: Float, val fall: Float, val cycle: Float)

private fun DrawScope.drawWeather(kind: WeatherKind, a: Anim) {
    when (kind) {
        WeatherKind.Sunny -> sun(32f, 32f, 14f, Sun, Color(0xFFFFB340), a)
        WeatherKind.Partly -> { sun(24f, 24f, 11f, Sun, Color(0xFFFFB340), a); cloud(14f, 26f, .95f, WhiteCloud, a.drift) }
        WeatherKind.Cloudy -> { cloud(18f, 10f, .82f, GrayCloud, -a.drift); cloud(8f, 24f, .95f, WhiteCloud, a.drift) }
        WeatherKind.Rain -> {
            cloud(8f, 6f, 1f, GrayCloud, a.drift)
            drop(22f, 44f, 9f, a.fall, 0f); drop(32f, 44f, 9f, a.fall, .28f); drop(42f, 44f, 9f, a.fall, .56f)
        }
        WeatherKind.Storm -> {
            cloud(8f, 4f, 1f, DarkCloud, a.drift)
            drop(18f, 42f, 8f, a.fall, .12f); drop(46f, 42f, 8f, a.fall, .44f)
            bolt(a.cycle)
        }
        WeatherKind.Drizzle -> {
            cloud(8f, 6f, 1f, LightCloud, a.drift)
            drop(20f, 44f, 5f, a.fall, 0f, 2.6f); drop(32f, 47f, 5f, a.fall, .3f, 2.6f); drop(44f, 44f, 5f, a.fall, .6f, 2.6f)
        }
        WeatherKind.ClearNight -> { moon(30f, 32f, 15f); star(51f, 16f, 3f, a.drift); star(55f, 31f, 2f, -a.drift) }
        WeatherKind.CloudyNight -> { moon(24f, 22f, 11f); cloud(14f, 26f, .95f, WhiteCloud, a.drift) }
        WeatherKind.Heat -> { sun(32f, 25f, 12f, HeatSun, Color(0xFFFF6B3D), a); wave(50f, a.drift); wave(57f, -a.drift) }
        WeatherKind.Wind -> {
            wind("M8 24 H36 a5 5 0 1 0 -5 -5", a.drift)
            wind("M8 34 H48 a5 5 0 1 1 -5 5", -a.drift)
            wind("M8 44 H30 a4 4 0 1 1 -4 4", a.drift)
        }
        WeatherKind.Fog -> listOf(
            Triple(22f, 14f to 50f, .95f), Triple(32f, 8f to 56f, .75f),
            Triple(42f, 12f to 52f, .55f), Triple(52f, 18f to 46f, .4f),
        ).forEachIndexed { i, (y, x, alpha) ->
            val dx = if (i % 2 == 0) a.drift else -a.drift
            drawLine(Fog.copy(alpha = alpha), Offset(x.first + dx, y), Offset(x.second + dx, y), 4.5f, StrokeCap.Round)
        }
    }
}

private fun gradient(colors: List<Color>, cx: Float, cy: Float, r: Float) =
    Brush.radialGradient(colors, center = Offset(cx - r * .24f, cy - r * .3f), radius = r * 1.5f)

private fun DrawScope.sun(cx: Float, cy: Float, r: Float, colors: List<Color>, ray: Color, a: Anim) {
    rotate(a.sway, pivot = Offset(cx, cy)) {
        repeat(8) { i ->
            val ang = i * PI / 4
            val c = cos(ang).toFloat()
            val s = sin(ang).toFloat()
            drawLine(ray, Offset(cx + c * (r + 4), cy + s * (r + 4)), Offset(cx + c * (r + 9), cy + s * (r + 9)), 3.2f, StrokeCap.Round)
        }
    }
    drawCircle(gradient(colors, cx, cy, r), r, Offset(cx, cy))
}

private fun DrawScope.cloud(x: Float, y: Float, s: Float, colors: List<Color>, dx: Float) {
    translate(x + dx, y) {
        scale(s, pivot = Offset.Zero) {
            drawPath(CloudPath, Brush.verticalGradient(colors, startY = 4f, endY = 30f))
        }
    }
}

private fun DrawScope.drop(x: Float, y: Float, len: Float, fall: Float, delay: Float, width: Float = 3.2f) {
    val p = (fall + delay) % 1f
    val dy = p * 8f
    val alpha = if (p < .7f) 1f else (1f - p) / .3f
    drawLine(Drop.copy(alpha = alpha), Offset(x, y + dy), Offset(x - len * .35f, y + len + dy), width, StrokeCap.Round)
}

private fun DrawScope.bolt(cycle: Float) {
    val alpha = when {
        cycle in .86f..<.88f -> .15f
        cycle in .90f..<.93f -> .3f
        else -> 1f
    }
    val path = Path().apply {
        moveTo(34f, 34f); lineTo(25f, 50f); lineTo(31f, 50f); lineTo(29f, 61f)
        lineTo(41f, 44f); lineTo(35f, 44f); lineTo(38f, 34f); close()
    }
    drawPath(path, Bolt.copy(alpha = alpha))
}

private fun DrawScope.moon(cx: Float, cy: Float, r: Float) {
    val disc = Path().apply { addOval(androidx.compose.ui.geometry.Rect(Offset(cx, cy), r)) }
    val bite = Path().apply { addOval(androidx.compose.ui.geometry.Rect(Offset(cx + r * .6f, cy - r * .4f), r * .85f)) }
    val crescent = Path().apply { op(disc, bite, androidx.compose.ui.graphics.PathOperation.Difference) }
    drawPath(crescent, gradient(Moon, cx, cy, r))
}

private fun DrawScope.star(x: Float, y: Float, s: Float, twinkle: Float) {
    val path = Path().apply {
        moveTo(x, y - s); lineTo(x + s * .3f, y - s * .3f); lineTo(x + s, y); lineTo(x + s * .3f, y + s * .3f)
        lineTo(x, y + s); lineTo(x - s * .3f, y + s * .3f); lineTo(x - s, y); lineTo(x - s * .3f, y - s * .3f); close()
    }
    drawPath(path, Color(0xFFF2F7FF).copy(alpha = .72f + twinkle / 5.5f))
}

private fun DrawScope.wave(y: Float, dx: Float) {
    val path = Path().apply {
        moveTo(14f + dx, y)
        repeat(4) { i ->
            val x = 14f + dx + i * 8f
            quadraticTo(x + 4f, y + if (i % 2 == 0) -4f else 4f, x + 8f, y)
        }
    }
    drawPath(path, Color(0xFFFF9F0A), style = Stroke(2.6f, cap = StrokeCap.Round))
}

private fun DrawScope.wind(d: String, dx: Float) {
    translate(dx, 0f) {
        drawPath(PathParser().parsePathString(d).toPath(), Color.White, style = Stroke(3.5f, cap = StrokeCap.Round))
    }
}
