package com.dimitrimednov.launchertv.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.LocalTextStyle
import androidx.tv.material3.Text
import com.dimitrimednov.launchertv.data.CurrentWeather
import com.dimitrimednov.launchertv.ui.icons.WeatherIcon
import com.dimitrimednov.launchertv.ui.theme.Motion
import com.dimitrimednov.launchertv.ui.theme.Palette
import kotlinx.coroutines.launch
import java.time.LocalDateTime

private val TextShadow = Shadow(Color.Black.copy(alpha = .45f), Offset(0f, 4f), 30f)

/**
 * Protector: hora grande abajo a la izquierda, fecha, clima y nombre del fondo. El fondo (foto o
 * video) es la misma capa del inicio, que aquí se ve sin degradados. Se abre tras 90 s sin tocar
 * nada o con Atrás dos veces; cualquier tecla lo cierra.
 */
@Composable
fun Screensaver(weather: CurrentWeather?, now: LocalDateTime, backgroundName: String) {
    val lines = remember { List(4) { Animatable(0f) } }
    LaunchedEffect(Unit) {
        lines.forEachIndexed { i, a -> launch { a.animateTo(1f, Motion.ease(1000, 300 + i * 160)) } }
    }
    fun Modifier.line(i: Int) = graphicsLayer { alpha = lines[i].value; translationY = (1 - lines[i].value) * 24f }

    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = .35f), .22f to Color.Transparent,
                    .55f to Color.Transparent, 1f to Color.Black.copy(alpha = .55f),
                ),
            ),
        )
        if (weather != null) {
            Row(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 44.dp, end = 72.dp)
                    .line(2)
                    .height(72.dp)
                    .background(Color.Black.copy(alpha = .38f), CircleShape)
                    .border(1.dp, Palette.glass(.18f), CircleShape)
                    .padding(horizontal = 30.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                WeatherIcon(weather.kind, 44.dp)
                Text("${weather.temperature}° ${weather.label} · Progreso, Yuc.", fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Column(Modifier.align(Alignment.BottomStart).padding(start = 96.dp, bottom = 84.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                formatClock(now, withPeriod = false),
                fontSize = 168.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-6).sp, lineHeight = 151.sp,
                style = LocalTextStyle.current.merge(shadow = TextShadow, fontFeatureSettings = "tnum"),
                modifier = Modifier.line(0).cssLineHeight(168.sp, 151.sp),
            )
            Text(
                formatLongDate(now), fontSize = 36.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFF2F2F7),
                style = LocalTextStyle.current.merge(shadow = TextShadow), modifier = Modifier.line(1),
            )
        }
        Column(
            Modifier.align(Alignment.BottomEnd).padding(end = 96.dp, bottom = 92.dp).line(3),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("Fondo", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Palette.Label, style = LocalTextStyle.current.merge(shadow = TextShadow))
            Text(backgroundName, fontSize = 30.sp, fontWeight = FontWeight.SemiBold, style = LocalTextStyle.current.merge(shadow = TextShadow))
        }
        Text(
            "Presiona cualquier botón para volver",
            fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Palette.glass(.75f),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 28.dp).line(3),
        )
    }
}
