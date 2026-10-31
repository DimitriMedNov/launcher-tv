package com.dimitrimednov.launchertv.ui.cuarto

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.dimitrimednov.launchertv.data.Weather
import com.dimitrimednov.launchertv.ui.home.WeatherState

/** Línea fina bajo el encabezado y la marca de acento a la izquierda. */
@Composable
fun HeaderRule() {
    Box(Modifier.offset(192.dp, 140.dp).size(1632.dp, 1.dp).background(city.ln))
    Box(Modifier.offset(192.dp, 138.dp).size(120.dp, 4.dp).background(city.ac))
}

/** Tres puntos a la izquierda: el de la sección actual es una píldora. */
@Composable
fun RailDots(current: CityScreen) {
    Column(
        Modifier.offset(96.dp, 160.dp).size(56.dp, 340.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CityScreen.entries.forEach { s ->
            val h by animateDpAsState(if (s == current) 32.dp else 10.dp, cityTween(240), label = "riel")
            Box(Modifier.size(10.dp, h).background(if (s == current) city.ac else city.lnS, RoundedCornerShape(5.dp)))
        }
    }
}

/** Emblema: luna creciente sobre la silueta de la ciudad, dentro de un círculo. */
@Composable
fun Emblem(size: Int = 64) {
    val c = city
    Canvas(Modifier.size(size.dp)) {
        val k = this.size.width / 64f
        val circle = Path().apply { addOval(androidx.compose.ui.geometry.Rect(0f, 0f, this@Canvas.size.width, this@Canvas.size.height)) }
        drawCircle(c.bg)
        clipPath(circle) {
            drawCircle(c.ac, 16f * k, Offset((14.08f + 16f) * k, (8.96f + 16f) * k))
            drawCircle(c.bg, 16f * k, Offset((23.04f + 16f) * k, (5.12f + 16f) * k))
            val h = this.size.height
            drawRect(c.tx, Offset(7.68f * k, h - 10.24f * k), Size(this.size.width - 15.36f * k, 10.24f * k))
            val tx = 33.28f * k; val tw = 7.68f * k; val th = 23.04f * k; val tb = h - 8.96f * k
            drawPath(
                Path().apply {
                    moveTo(tx + tw / 2, tb - th); lineTo(tx + tw, tb - th + th * .22f); lineTo(tx + tw, tb)
                    lineTo(tx, tb); lineTo(tx, tb - th + th * .22f); close()
                },
                c.tx,
            )
            drawRect(c.tx, Offset(19.2f * k, tb - 12.8f * k), Size(8.96f * k, 12.8f * k))
        }
        drawCircle(c.ac, this.size.width / 2 - 1.5f * k, style = Stroke(3f * k))
    }
}

/** Encabezado izquierdo: emblema, título y avisos (sin conexión, modo Mover). */
@Composable
fun HeaderLeft(title: String, offline: Boolean, wifiFocused: Boolean, moving: Boolean) {
    Row(
        Modifier.offset(192.dp, 54.dp).height(72.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        Emblem()
        CText(title, 60, weight = FontWeight.Bold, condensed = true, upper = true, spacing = .03f, lineHeight = 1f)
        if (offline) {
            Row(
                Modifier.height(56.dp).border(1.dp, city.err, R4).padding(start = 18.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CText("Sin conexión", 28, color = city.err, weight = FontWeight.Bold, condensed = true, upper = true)
                CityButton("Abrir ajustes de wifi", wifiFocused, height = 46, fontSize = 24, padding = 20)
            }
        }
        if (moving) CText("Moviendo · OK guarda · Atrás cancela", 26, color = city.ac)
    }
}

/** Encabezado derecho: temporizador, modo noche, clima y hora. */
@Composable
fun HeaderRight(
    timerLeft: String?,
    night: Boolean,
    nightShort: String,
    weather: WeatherState,
    online: Boolean,
    weatherFocused: Boolean,
    clock: Pair<String, String>,
    modifier: Modifier = Modifier,
) {
    val c = city
    Row(
        modifier.padding(top = 54.dp, end = 96.dp).height(72.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (timerLeft != null) {
            Chip(border = c.ac) {
                CText("Apaga en", 24, color = c.mu)
                CText(timerLeft, 30, color = c.ac, weight = FontWeight.SemiBold, condensed = true, tabular = true)
            }
        }
        Chip {
            Box(
                Modifier.size(16.dp).border(2.dp, c.ac, CircleShape)
                    .background(Brush.horizontalGradient(0f to c.ac, .5f to c.ac, .5f to Color.Transparent), CircleShape),
            )
            CText(if (night) "Noche" else "Día", 28, weight = FontWeight.SemiBold, condensed = true, upper = true)
            CText(nightShort, 24, color = c.mu)
        }
        WeatherChip(weather, online, weatherFocused)
        Row(Modifier.padding(start = 14.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CText(clock.first, 60, weight = FontWeight.SemiBold, condensed = true, tabular = true, lineHeight = 1f)
            CText(clock.second, 28, color = c.ac, weight = FontWeight.SemiBold, condensed = true, upper = true, modifier = Modifier.padding(bottom = 6.dp))
        }
    }
}

@Composable
private fun Chip(modifier: Modifier = Modifier, border: Color = city.ln, content: @Composable () -> Unit) {
    Row(
        modifier.height(52.dp).background(city.sf, R4).border(1.dp, border, R4).padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) { content() }
}

@Composable
private fun WeatherChip(weather: WeatherState, online: Boolean, focused: Boolean) {
    val c = city
    val scale by animateFloatAsState(if (focused) 1.06f else 1f, cityTween(240), label = "clima")
    val failed = weather is WeatherState.Failed && online
    Row(
        Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .focusGlow(focused, Color.Transparent, c.glow)
            .height(52.dp)
            .background(if (focused) c.sf2 else c.sf, R4)
            .border(1.dp, if (focused) c.ac else if (failed) c.err else c.ln, R4)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val (big, small) = when {
            !online -> "-- °C" to "Sin datos"
            weather is WeatherState.Ready -> "${weather.weather.current.temperature} °C" to weather.weather.current.label
            weather is WeatherState.Failed -> "Error" to if (focused) "OK: reintentar" else "Reintentar ↑"
            else -> null to null
        }
        if (big == null || small == null) {
            Bone(170, 24)
        } else {
            CText(big, 30, weight = FontWeight.SemiBold, condensed = true, tabular = true)
            CText(small, 24, color = if (failed || !online) c.err else c.mu)
        }
    }
}

/** Menú lateral (se abre con izquierda). */
@Composable
fun SideMenu(open: Boolean, index: Int, current: CityScreen) {
    val c = city
    val p by animateFloatAsState(if (open) 1f else 0f, cityTween(280), label = "menú")
    if (p == 0f) return
    Column(
        Modifier
            .offset(96.dp, 54.dp)
            .graphicsLayer { translationX = (1 - p) * -560f * density; alpha = p }
            .size(460.dp, 972.dp)
            .background(c.sf2, R4)
            .border(1.dp, c.lnS, R4)
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CText("MENÚ", 24, color = c.mu, tabular = true, modifier = Modifier.padding(bottom = 8.dp))
        CityScreen.entries.forEachIndexed { i, s ->
            val f = open && index == i
            val dx by animateDpAsState(if (f) 6.dp else 0.dp, cityTween(240), label = "item")
            Column(
                Modifier
                    .offset { IntOffset(dx.roundToPx(), 0) }
                    .focusGlow(f, Color.Transparent, c.glow)
                    .fillMaxWidth()
                    .height(108.dp)
                    .background(if (f) c.sf else Color.Transparent, R4)
                    .border(1.dp, if (f) c.ac else if (s == current) c.lnS else Color.Transparent, R4)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
            ) {
                CText(s.label, 34, color = if (f) c.ac else c.tx, weight = FontWeight.SemiBold)
                CText(s.sub, 24, color = c.mu)
            }
        }
        Spacer(Modifier.weight(1f))
        CText("Derecha o Atrás para cerrar", 24, color = c.mu, tabular = true)
    }
}

/** Aviso abajo al centro. */
@Composable
fun CityToast(text: String?, modifier: Modifier = Modifier) {
    var last by remember { mutableStateOf("") }
    if (text != null) last = text
    AnimatedVisibility(
        text != null, modifier.padding(bottom = 72.dp),
        enter = fadeIn(tween(220)) + androidx.compose.animation.slideInVertically(cityTween(260)) { 16 },
        exit = fadeOut(tween(220)),
    ) {
        Box(Modifier.background(city.sf2, R4).border(1.dp, city.lnS, R4).padding(horizontal = 28.dp, vertical = 16.dp)) {
            CText(last, 26)
        }
    }
}

/** Ventana de información de una app. */
@Composable
fun InfoWindow(info: InfoBox?, name: String, color: Color, rows: List<Pair<String, String>>) {
    val c = city
    AnimatedVisibility(
        info != null, Modifier.offset(460.dp, 250.dp),
        enter = fadeIn(cityTween(220)) + scaleIn(cityTween(220), initialScale = .88f),
        exit = fadeOut(tween(160)) + scaleOut(tween(160), targetScale = .95f),
    ) {
        Column(
            Modifier.width(1000.dp).background(c.sf2, R4).border(1.dp, c.lnS, R4).padding(48.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Box(Modifier.size(56.dp).background(color, RoundedCornerShape(12.dp)))
                CText(name, 56, weight = FontWeight.SemiBold)
            }
            Column(Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(c.ln))
                rows.forEach { (k, v) ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        CText(k, 26, color = c.mu)
                        CText(v, 26, modifier = Modifier.padding(start = 24.dp))
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(c.ln))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                InfoButtons.forEachIndexed { i, label -> CityButton(label, info?.button == i) }
            }
        }
    }
}

/** Texto del clima largo para el protector. */
fun weatherLong(weather: Weather?, online: Boolean) = when {
    !online -> "Clima sin conexión"
    weather == null -> "Progreso, Yuc."
    else -> "${weather.current.temperature} °C · ${weather.current.label} · Progreso"
}
