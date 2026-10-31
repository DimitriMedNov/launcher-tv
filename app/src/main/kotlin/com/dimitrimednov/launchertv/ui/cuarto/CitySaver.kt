package com.dimitrimednov.launchertv.ui.cuarto

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/** Edificios del horizonte del protector: ancho, alto y altura de sus dos ventanas (0 = sin ventanas). */
private val Skyline = listOf(
    Triple(70, 120, 0), Triple(40, 170, 0), Triple(90, 90, 23), Triple(50, 150, 0), Triple(30, 70, 0),
    Triple(80, 180, 45), Triple(60, 110, 0), Triple(44, 140, 0), Triple(100, 80, 20), Triple(36, 165, 0),
    Triple(70, 100, 0), Triple(52, 130, 33), Triple(28, 60, 0), Triple(84, 175, 0), Triple(46, 95, 24),
    Triple(64, 150, 0), Triple(38, 120, 0), Triple(90, 85, 21), Triple(56, 160, 0), Triple(40, 105, 0),
    Triple(76, 135, 34), Triple(30, 75, 0), Triple(66, 170, 0), Triple(48, 90, 23), Triple(82, 125, 0),
    Triple(34, 150, 0), Triple(60, 100, 25),
)

private val SaverSpots = listOf(150 to 110, 760 to 470, 230 to 500, 820 to 140, 470 to 300)

/**
 * Protector: horizonte de la ciudad, luna, hora enorme, fecha y clima. La hora cambia de lugar
 * una vez por minuto con un fundido para no marcar la pantalla.
 */
@Composable
fun CitySaver(clock: Pair<String, String>, date: String, weather: String) {
    val c = city
    var spot by remember { mutableIntStateOf(0) }
    val fade = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            fade.animateTo(0f, tween(900))
            spot = (spot + 1) % SaverSpots.size
            fade.animateTo(1f, tween(900))
        }
    }
    Box(Modifier.fillMaxSize().background(c.svbg)) {
        Row(Modifier.align(Alignment.BottomStart).fillMaxWidth(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Skyline.forEach { (w, h, win) ->
                Box(
                    Modifier.weight(w.toFloat()).height(h.dp).background(c.sf).drawBehind {
                        if (win > 0) {
                            val ws = Size(8.dp.toPx(), 11.dp.toPx())
                            drawRect(c.ac.copy(alpha = .45f), Offset(size.width * .30f, win.dp.toPx()), ws)
                            drawRect(c.ac.copy(alpha = .30f), Offset(size.width * .62f, (win * 2 - 1).dp.toPx()), ws)
                        }
                    },
                )
            }
        }
        val (x, y) = SaverSpots[spot]
        Column(Modifier.offset(x.dp, y.dp).width(IntrinsicSize.Max).graphicsLayer { alpha = fade.value * if (c.night) .75f else 1f }) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(44.dp)) {
                Moon(210)
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CText(clock.first, 250, color = c.svtx, weight = FontWeight.Bold, condensed = true, tabular = true, spacing = -.01f, lineHeight = .85f)
                    CText(clock.second, 64, color = c.ac, weight = FontWeight.Bold, condensed = true, upper = true, modifier = Modifier.padding(bottom = 18.dp))
                }
            }
            Box(Modifier.padding(top = 28.dp).fillMaxWidth().height(1.dp).background(c.lnS))
            Row(Modifier.padding(top = 20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                CText(date, 40, color = c.svtx, weight = FontWeight.SemiBold, condensed = true, upper = true, spacing = .04f)
                Box(Modifier.size(8.dp).graphicsLayer { rotationZ = 45f }.background(c.ac))
                CText(weather, 32, color = c.svtx)
            }
        }
    }
}

/**
 * Tormenta encima de todo: relámpagos, lluvia y murciélagos. El diseño trae la lluvia sin parar;
 * aquí llega en aguaceros de unos segundos para que la tele no repinte todo sin descanso.
 */
@Composable
fun Storm(dim: Boolean) {
    val c = city
    val flash = remember { Animatable(0f) }
    val rain = remember { Animatable(0f) }
    val bats = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch {
            delay(1_200)
            while (true) {
                launch { rain.snapTo(0f); rain.animateTo(1f, tween(RAIN_MS, easing = LinearEasing)) }
                repeat(2) {
                    delay(Random.nextLong(800, 3_000))
                    flash.snapTo(0f)
                    flash.animateTo(1f, tween(1_100, easing = LinearEasing))
                    flash.snapTo(0f)
                }
                delay(RAIN_MS + Random.nextLong(18_000, 32_000))
            }
        }
        launch {
            delay(600)
            while (true) {
                bats.snapTo(0f)
                bats.animateTo(1f, tween(BAT_MS, easing = LinearEasing))
                bats.snapTo(0f)
                delay(Random.nextLong(14_000, 22_000))
            }
        }
    }
    // Capa propia: al animarse solo se repinta la tormenta, no toda la interfaz de abajo.
    Canvas(Modifier.fillMaxSize().graphicsLayer()) {
        val k = size.width / 1920f
        if (flash.value > 0f) {
            val o = flashOpacity(flash.value) * if (dim) .5f else 1f
            drawRect(Brush.verticalGradient(listOf(Color(0xFFF4F1E6).copy(alpha = o), Color.Transparent), 0f, 400 * k), size = Size(size.width, 400 * k))
        }
        if (rain.value in 0.001f..0.999f) drawRain(rain.value * RAIN_MS, k)
        if (bats.value in 0.001f..0.999f) drawBats(bats.value * BAT_MS, k, c.bat)
    }
}

private const val RAIN_MS = 7_000
private const val BAT_MS = 6_000

/** Curva del relámpago del diseño: dos destellos (0.32 y 0.18) dentro de 1.1 s. */
private fun flashOpacity(t: Float): Float = when {
    t < .08f -> t / .08f * .32f
    t < .2f -> (1 - (t - .08f) / .12f) * .32f
    t < .3f -> (t - .2f) / .1f * .18f
    t < .5f -> (1 - (t - .3f) / .2f) * .18f
    else -> 0f
}

private fun DrawScope.drawRain(ms: Float, k: Float) {
    val brushCache = HashMap<Int, Brush>()
    for (i in 0 until 46) {
        val period = 900 + (i % 5) * 120
        val start = (i * 53) % 900
        // La última vuelta de cada gota termina antes del final del aguacero.
        if (ms < start || ms > RAIN_MS - period) continue
        val p = ((ms - start) % period) / period
        val alpha = when { p < .1f -> p / .1f; p > .85f -> (1 - p) / .15f; else -> 1f }
        val x = ((i * 397) % 1880 + 20) * k
        val h = (40 + (i * 13) % 50) * k
        val top = (-120 - (i * 71) % 300) * k + p * 1300 * k
        val brush = brushCache.getOrPut(h.toInt()) {
            Brush.verticalGradient(listOf(Color.Transparent, Color(0xFFC8D7EB).copy(alpha = .55f)), 0f, h)
        }
        rotate(10f, Offset(x, top)) {
            translate(x, top) { drawRect(brush, size = Size(2 * k, h), alpha = alpha) }
        }
    }
}

private val BatScale = listOf(1f, .7f, .85f, .6f, 1.1f, .75f, .9f, .65f)
private val BatShape = listOf(
    0f to .30f, .12f to 0f, .20f to .35f, .32f to .22f, .42f to .48f, .47f to .30f, .50f to .42f, .53f to .30f,
    .58f to .48f, .68f to .22f, .80f to .35f, .88f to 0f, 1f to .30f, .86f to .70f, .70f to .58f, .60f to 1f,
    .50f to .78f, .40f to 1f, .30f to .58f, .14f to .70f,
)

private fun DrawScope.drawBats(ms: Float, k: Float, color: Color) {
    BatScale.forEachIndexed { i, s ->
        val dur = 4_200 + (i % 4) * 500
        val t = (ms - i * 180) / dur
        if (t <= 0f || t >= 1f) return@forEachIndexed
        val wiggle = if (i % 2 == 1) listOf(0f, -40f, 20f, -30f) else listOf(0f, 30f, -35f, 10f)
        val seg = (t * 3).toInt().coerceAtMost(2)
        val local = t * 3 - seg
        val dy = wiggle[seg] + (wiggle[seg + 1] - wiggle[seg]) * local
        val x = (1960 + i * 70 - 2300 * t) * k
        val y = (150 + (i * 47) % 230 + dy) * k
        val w = 64 * s * k
        val h = 26 * s * k
        val flapMs = 260 + (i % 3) * 40
        val phase = ((ms - i * 180) % flapMs) / flapMs
        val flap = 1f - .65f * (if (phase < .5f) phase * 2 else 2 - phase * 2)
        val path = Path().apply {
            BatShape.forEachIndexed { j, (px, py) ->
                val yy = h / 2 + (py * h - h / 2) * flap
                if (j == 0) moveTo(px * w, yy) else lineTo(px * w, yy)
            }
            close()
        }
        translate(x, y) { drawPath(path, color) }
    }
}
