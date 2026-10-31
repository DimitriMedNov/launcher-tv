package com.dimitrimednov.launchertv.ui.cuarto

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dimitrimednov.launchertv.data.DayForecast
import com.dimitrimednov.launchertv.data.LauncherApp
import com.dimitrimednov.launchertv.ui.home.WeatherState
import java.time.LocalDate

class SettingsModel(
    val state: CityState,
    val focus: Pair<SettingsRow, Int>,
    val timer: Pair<Int, Long>?,
    val now: Long,
    val nightMode: NightMode,
    val night: Boolean,
    val weather: WeatherState,
    val online: Boolean,
    val hidden: List<LauncherApp>,
    val wifiSub: String,
)

@Composable
fun SettingsContent(m: SettingsModel) {
    val c = city
    val (row, col) = m.focus
    fun f(r: SettingsRow, i: Int) = m.state.base && row == r && col == i

    Column(Modifier.offset(192.dp, 164.dp).width(1000.dp)) {
        SectionLabel("ACCESOS RÁPIDOS")
        Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            QuickSetting.entries.forEachIndexed { i, q ->
                val sub = when (q) {
                    QuickSetting.Wifi -> m.wifiSub
                    QuickSetting.Picture -> "Ajustes de imagen"
                    QuickSetting.Sound -> "Bocinas y salida"
                    QuickSetting.Android -> "Sistema"
                }
                QuickCard(q.label, sub, f(SettingsRow.Quick, i))
            }
        }

        SectionLabel("TEMPORIZADOR PARA DORMIR", Modifier.padding(top = 32.dp))
        TimerPanel(m) { f(SettingsRow.Timer, it) }

        SectionLabel("MODO NOCHE · AHORA: ${if (m.night) "NOCHE" else "DÍA"}", Modifier.padding(top = 32.dp))
        Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            NightMode.entries.forEachIndexed { i, mode ->
                val sel = m.nightMode == mode
                val focused = f(SettingsRow.Night, i)
                CityButton(mode.label, focused, Modifier.weight(1f), height = 96, padding = 22, selected = sel) {
                    val tc = if (focused) c.onAc else c.tx
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (sel) Box(Modifier.size(10.dp).background(tc, RoundedCornerShape(2.dp)))
                            CText(mode.label, 28, color = tc, weight = FontWeight.Medium)
                        }
                        CText(mode.sub, 24, color = tc.copy(alpha = .85f), tabular = true)
                    }
                }
            }
        }
    }

    Column(Modifier.offset(1232.dp, 164.dp).width(592.dp)) {
        ForecastCard(m.weather, m.online, f(SettingsRow.Forecast, 0))
        Row(Modifier.fillMaxWidth().padding(top = 32.dp).height(32.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            CText("APPS OCULTAS", 24, color = c.mu, tabular = true)
            CText("${m.hidden.size}", 24, color = c.mu, tabular = true)
        }
        if (m.hidden.isEmpty()) {
            Column(
                Modifier.padding(top = 12.dp).fillMaxWidth().dashedBorder(c.lnS).padding(horizontal = 28.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CText("No hay apps ocultas", 28, weight = FontWeight.SemiBold)
                CText("Mantén OK sobre una app del inicio y elige Ocultar.", 24, color = c.mu)
                CityButton("Volver al inicio", f(SettingsRow.Hidden, 0), Modifier.padding(top = 8.dp))
            }
        } else {
            val focusCol = if (row == SettingsRow.Hidden) col else 0
            val shift by animateDpAsState((maxOf(0, focusCol - 4) * 76).dp, cityTween(300), label = "lista")
            Box(Modifier.padding(top = 12.dp).fillMaxWidth().height(348.dp).clipToBounds()) {
                m.hidden.forEachIndexed { i, app ->
                    HiddenRow(app, f(SettingsRow.Hidden, i), Modifier.offset { IntOffset(0, ((i * 76).dp - shift).roundToPx()) })
                }
            }
        }
    }
}

@Composable
private fun QuickCard(name: String, sub: String, focused: Boolean) {
    val c = city
    val scale by animateFloatAsState(if (focused) 1.06f else 1f, cityTween(240), label = "acceso")
    Column(
        Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .focusGlow(focused, c.ac, c.glow)
            .size(235.dp, 148.dp)
            .background(if (focused) c.sf2 else c.sf, R4)
            .border(1.dp, if (focused) c.ac else c.ln, R4)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        CText(name, 28, weight = FontWeight.SemiBold, lineHeight = 1.15f, maxLines = 2)
        CText(sub, 24, color = c.mu, tabular = true)
    }
}

@Composable
private fun TimerPanel(m: SettingsModel, focused: (Int) -> Boolean) {
    val c = city
    val running = m.timer
    val left = running?.let { (it.second - m.now).coerceAtLeast(0) }
    Column(
        Modifier.padding(top = 12.dp).fillMaxWidth().height(240.dp).background(c.sf, R4).border(1.dp, c.lnS, R4).padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CText("Apagar la tele sola", 32, weight = FontWeight.SemiBold)
                CText(
                    if (running != null) "Se apaga a las ${clockOf(running.second).let { "${it.first} ${it.second}" }}"
                    else "Elige cuánto tiempo y la tele se apaga sola.",
                    24, color = c.mu,
                )
            }
            if (left != null) CText(mmssOf(left), 64, color = c.ac2, weight = FontWeight.Medium, tabular = true, lineHeight = 1f)
            else CText("Apagado", 40, color = c.mu, weight = FontWeight.Medium, lineHeight = 1f)
        }
        Box(Modifier.fillMaxWidth().height(6.dp).background(c.ln, RoundedCornerShape(4.dp))) {
            val p = if (running != null && left != null) left / (running.first * 60_000f) else 0f
            val w by animateFloatAsState(p.coerceIn(0f, 1f), tween(900), label = "temporizador")
            Box(Modifier.fillMaxWidth(w).fillMaxHeight().background(c.ac2))
        }
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TimerOptions.forEachIndexed { i, min ->
                val sel = (running?.first ?: 0) == min
                val f = focused(i)
                CityButton(if (min == 0) "Apagado" else "$min min", f, Modifier.weight(1f), padding = 16, selected = sel) {
                    val tc = if (f) c.onAc else c.tx
                    if (sel) Box(Modifier.size(10.dp).background(tc, RoundedCornerShape(2.dp)))
                    CText(if (min == 0) "Apagado" else "$min min", 26, color = tc, weight = FontWeight.Medium, condensed = true, upper = true, spacing = .05f)
                }
            }
        }
    }
}

@Composable
private fun HiddenRow(app: LauncherApp, focused: Boolean, modifier: Modifier) {
    val c = city
    Row(
        modifier.fillMaxWidth().height(64.dp).background(if (focused) c.sf2 else c.sf, R4)
            .border(1.dp, if (focused) c.ac else c.ln, R4).padding(start = 20.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(Modifier.size(22.dp).background(appColor(app), RoundedCornerShape(5.dp)))
        CText(app.name, 28, weight = FontWeight.Medium)
        Spacer(Modifier.weight(1f))
        CityButton("Mostrar", focused, height = 48, fontSize = 24, padding = 20)
    }
}

@Composable
private fun ForecastCard(weather: WeatherState, online: Boolean, retryFocused: Boolean) {
    val c = city
    Column(
        Modifier.fillMaxWidth().height(430.dp).background(c.sf, R4).border(1.dp, c.ln, R4).padding(horizontal = 28.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            CText("Pronóstico", 28, weight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(26.dp, 3.dp).background(c.ac)); CText("Máx", 24)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Canvas(Modifier.size(26.dp, 3.dp)) {
                        drawLine(c.ac2, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), size.height, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 5f)))
                    }
                    CText("Mín", 24)
                }
            }
        }
        val days = (weather as? WeatherState.Ready)?.weather?.days?.take(5)
        when {
            online && days != null && days.isNotEmpty() -> ForecastChart(days)
            online && weather is WeatherState.Loading -> Row(
                Modifier.fillMaxWidth().weight(1f).padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(40.dp), verticalAlignment = Alignment.Bottom,
            ) {
                repeat(5) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.fillMaxWidth().height(150.dp).background(c.sf2, RoundedCornerShape(8.dp)))
                        Box(Modifier.fillMaxWidth(.6f).height(24.dp).background(c.sf2, RoundedCornerShape(4.dp)))
                    }
                }
            }
            else -> Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically)) {
                Box(Modifier.border(1.dp, c.err, R4).padding(horizontal = 12.dp, vertical = 2.dp)) {
                    CText(if (!online) "SIN CONEXIÓN" else "ERROR", 24, color = c.err, tabular = true)
                }
                CText(if (!online) "No se pudo actualizar el pronóstico" else "El servicio del clima no respondió", 32, weight = FontWeight.SemiBold, maxLines = 2)
                CText("Lo demás de Ajustes sigue funcionando.", 24, color = c.mu)
                CityButton("Reintentar", retryFocused, Modifier.padding(top = 8.dp))
            }
        }
    }
}

private val DayShort = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

/** Línea de máximas (sólida) y mínimas (punteada) de 5 días, como la gráfica del diseño. */
@Composable
private fun ForecastChart(days: List<DayForecast>) {
    val c = city
    val measurer = rememberTextMeasurer()
    val hi = days.maxOf { it.max } + 3
    val lo = minOf(days.minOf { it.min } - 2, hi - 14)
    val today = LocalDate.now()
    Canvas(Modifier.size(536.dp, 320.dp)) {
        val k = size.width / 536f
        fun xs(i: Int) = (48 + i * 110) * k
        fun ys(t: Int) = (50 + (hi - t).toFloat() / (hi - lo) * 190) * k
        val grid = (lo + 1 until hi).filter { it % 5 == 0 }.map { ys(it) } + 280 * k
        grid.forEach { y -> drawLine(c.ln, Offset(0f, y), Offset(size.width, y), 1.dp.toPx()) }
        fun line(sel: (DayForecast) -> Int) = Path().apply {
            days.forEachIndexed { i, d -> if (i == 0) moveTo(xs(i), ys(sel(d))) else lineTo(xs(i), ys(sel(d))) }
        }
        drawPath(line { it.max }, c.ac, style = Stroke(3.dp.toPx(), join = StrokeJoin.Round))
        drawPath(line { it.min }, c.ac2, style = Stroke(3.dp.toPx(), join = StrokeJoin.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8 * k, 7 * k))))
        fun label(text: String, x: Float, baseline: Float, sizeSp: Int, color: Color, weight: FontWeight = FontWeight.Normal) {
            val r = measurer.measure(text, TextStyle(fontFamily = Barlow, fontSize = sizeSp.sp, fontWeight = weight, color = color, fontFeatureSettings = "tnum"))
            drawText(r, topLeft = Offset(x - r.size.width / 2f, baseline - r.firstBaseline))
        }
        days.forEachIndexed { i, d ->
            val x = xs(i)
            drawCircle(c.bg, 6 * k, Offset(x, ys(d.max))); drawCircle(c.ac, 6 * k, Offset(x, ys(d.max)), style = Stroke(3 * k))
            drawCircle(c.bg, 6 * k, Offset(x, ys(d.min))); drawCircle(c.ac2, 6 * k, Offset(x, ys(d.min)), style = Stroke(3 * k))
            label("${d.max}°", x, ys(d.max) - 18 * k, 26, c.tx, FontWeight.Medium)
            label("${d.min}°", x, ys(d.min) + 38 * k, 24, c.mu)
            label(if (d.date == today) "Hoy" else DayShort[d.date.dayOfWeek.value - 1], x, 312 * k, 24, c.tx)
        }
    }
}

/** Borde punteado de 1 px (la caja de "No hay apps ocultas"). */
private fun Modifier.dashedBorder(color: Color): Modifier = drawBehind {
    drawRoundRect(
        color, cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
        style = Stroke(1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))),
    )
}
