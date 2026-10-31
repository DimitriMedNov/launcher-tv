package com.dimitrimednov.launchertv.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.dimitrimednov.launchertv.data.DayForecast
import com.dimitrimednov.launchertv.ui.icons.WeatherIcon
import com.dimitrimednov.launchertv.ui.theme.Layout
import com.dimitrimednov.launchertv.ui.theme.Motion
import com.dimitrimednov.launchertv.ui.theme.Palette
import java.time.LocalDate
import java.time.format.TextStyle

private const val ROW_STEP = 96

/** Sección Ajustes rápidos: lista con indicador blanco que se desliza y pronóstico de 3 días. */
@Composable
fun SettingsSection(state: NavState, wifiOn: Boolean, hiddenCount: Int, forecast: List<DayForecast>?) {
    val active = state.zone == Zone.Settings && state.panel == null
    Text(
        "Ajustes rápidos", fontSize = 96.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-3).sp, lineHeight = 96.sp,
        modifier = Modifier.offset(Layout.ContentLeft, Layout.SectionTop).cssLineHeight(96.sp, 96.sp),
    )
    Box(Modifier.offset(Layout.ContentLeft, 300.dp).width(820.dp)) {
        val indicatorY by animateDpAsState((state.setting * ROW_STEP).dp, spring(dampingRatio = .62f, stiffness = 420f), label = "indicador")
        val indicator by animateFloatAsState(if (active) 1f else 0f, tween(250), label = "indicador visible")
        Box(
            Modifier
                .offsetDp({ 0.dp }, { indicatorY })
                .fillMaxWidth()
                .height(88.dp)
                .graphicsLayer { alpha = indicator; scaleX = .96f + .04f * indicator; scaleY = .96f + .04f * indicator }
                .shadow(22.dp, CircleShape)
                .background(Palette.White, CircleShape),
        )
        SettingItem.entries.forEachIndexed { i, item ->
            SettingRow(
                item = item,
                focused = active && i == state.setting,
                value = when (item) {
                    SettingItem.Wifi -> null
                    SettingItem.HiddenApps -> if (hiddenCount == 0) "Ninguna" else "$hiddenCount"
                    else -> "Abrir"
                },
                wifiOn = wifiOn,
                modifier = Modifier.offset(y = (i * ROW_STEP).dp),
            )
        }
    }
    if (forecast != null) Forecast(forecast)
}

@Composable
private fun SettingRow(item: SettingItem, focused: Boolean, value: String?, wifiOn: Boolean, modifier: Modifier) {
    val fg by animateColorAsState(if (focused) Palette.Bg else Palette.White, tween(220), label = "texto ajuste")
    val valueColor by animateColorAsState(if (focused) Palette.OnWhiteMuted else Palette.Tertiary, tween(220), label = "valor")
    val bg by animateColorAsState(if (focused) Palette.White.copy(alpha = 0f) else Palette.glass(.06f), tween(220), label = "fondo ajuste")
    val iconScale by animateFloatAsState(if (focused) 1.15f else 1f, Motion.bouncy(), label = "ícono ajuste")
    val iconTurn by animateFloatAsState(if (focused) -8f else 0f, Motion.bouncy(), label = "giro ajuste")
    Row(
        modifier.fillMaxWidth().height(88.dp).background(bg, CircleShape).padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Icon(item.icon, contentDescription = null, tint = fg, modifier = Modifier.size(36.dp).scale(iconScale).rotate(iconTurn))
        Text(item.label, color = fg, fontSize = 30.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 1)
        if (item == SettingItem.Wifi) Toggle(wifiOn)
        if (value != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(value, color = valueColor, fontSize = 28.sp, maxLines = 1)
                // Como en el diseño: la flecha solo aparece en la fila enfocada.
                val arrow by animateFloatAsState(if (focused) 1f else 0f, tween(200), label = "flecha")
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = valueColor,
                    modifier = Modifier.size(32.dp).graphicsLayer { alpha = arrow },
                )
            }
        }
    }
}

/** Interruptor de solo lectura: muestra si el wifi está prendido; OK abre los ajustes de wifi. */
@Composable
private fun Toggle(on: Boolean) {
    val knob by animateDpAsState(if (on) 30.dp else 0.dp, Motion.bouncy(), label = "perilla")
    val track by animateColorAsState(if (on) Palette.Accent else Palette.ToggleOff, tween(300), label = "riel")
    Box(Modifier.size(68.dp, 38.dp).background(track, RoundedCornerShape(19.dp)).padding(4.dp)) {
        Box(Modifier.offsetDp({ knob }, { 0.dp }).size(30.dp).shadow(2.dp, CircleShape).background(Palette.White, CircleShape))
    }
}

@Composable
private fun Forecast(days: List<DayForecast>) {
    Column(Modifier.offset(1120.dp, 300.dp).width(600.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Pronóstico · Progreso, Yuc.", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Palette.Label)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            days.take(3).forEachIndexed { i, day ->
                val enter = remember { Animatable(0f) }
                LaunchedEffect(Unit) { enter.animateTo(1f, Motion.ease(620, 200 + i * 60)) }
                Column(
                    Modifier
                        .weight(1f)
                        .graphicsLayer { alpha = enter.value; translationY = (1 - enter.value) * 30f }
                        .background(Palette.glass(.07f), RoundedCornerShape(32.dp))
                        .border(1.dp, Palette.glass(.08f), RoundedCornerShape(32.dp))
                        .padding(vertical = 24.dp, horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(dayName(day.date, i), fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    WeatherIcon(day.kind, 64.dp)
                    Row {
                        Text("${day.max}° ", fontSize = 26.sp)
                        Text("${day.min}°", fontSize = 26.sp, color = Palette.Tertiary)
                    }
                }
            }
        }
    }
}

private fun dayName(date: LocalDate, index: Int) = when (index) {
    0 -> "Hoy"
    1 -> "Mañana"
    else -> date.dayOfWeek.getDisplayName(TextStyle.SHORT, SpanishMx).trimEnd('.')
}
