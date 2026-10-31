package com.dimitrimednov.launchertv.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.LocalTextStyle
import androidx.tv.material3.Text
import com.dimitrimednov.launchertv.data.CurrentWeather
import com.dimitrimednov.launchertv.ui.icons.WeatherIcon
import com.dimitrimednov.launchertv.ui.theme.Layout
import com.dimitrimednov.launchertv.ui.theme.Palette
import java.time.LocalDateTime

/** Píldora de arriba a la derecha: clima y hora. Sin atajos de teclas: el control se entiende solo. */
@Composable
fun TopBar(weather: CurrentWeather?, now: LocalDateTime) {
    Box(Modifier.width(1920.dp).padding(top = Layout.ChromeTop, end = Layout.ChromeRight), contentAlignment = Alignment.TopEnd) {
        Row(
            modifier = Modifier
                .height(72.dp)
                // El diseño usa desenfoque detrás de la píldora; Android no lo permite sobre lo que hay
                // detrás, así que se oscurece un poco para que el texto se lea igual sobre el cielo claro.
                .background(Palette.Bg.copy(alpha = .38f), CircleShape)
                .background(Palette.glass(.08f), CircleShape)
                .border(1.dp, Palette.glass(.12f), CircleShape)
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Si el clima falla, solo se oculta este bloque (estado de error del brief).
            if (weather != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    WeatherIcon(weather.kind, 40.dp)
                    Text("${weather.temperature}° ${weather.label}", fontSize = 28.sp, color = Palette.Label, maxLines = 1)
                }
                Box(Modifier.width(1.dp).height(32.dp).background(Palette.glass(.25f)))
            }
            Text(
                formatClock(now),
                fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = (-.4).sp, maxLines = 1,
                style = LocalTextStyle.current.merge(fontFeatureSettings = "tnum"),
            )
        }
    }
}
