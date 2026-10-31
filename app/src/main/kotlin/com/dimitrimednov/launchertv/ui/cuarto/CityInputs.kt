package com.dimitrimednov.launchertv.ui.cuarto

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dimitrimednov.launchertv.data.TvInput
import kotlinx.coroutines.delay

@Composable
fun InputsContent(inputs: List<TvInput>?, state: CityState) {
    val c = city
    Column(Modifier.offset(192.dp, 168.dp).width(1632.dp), verticalArrangement = Arrangement.spacedBy(28.dp)) {
        CText("Elige qué ver. El punto lleno indica que hay señal.", 28, color = c.mu)
        when {
            inputs == null -> Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                repeat(3) {
                    Column(
                        Modifier.size(522.dp, 440.dp).background(c.sf, R4).border(1.dp, c.ln, R4).padding(36.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Bone(132, 60, radius = 4)
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { Bone(240, 56, radius = 8); Bone(300, 28, radius = 4) }
                        Bone(180, 26, radius = 4)
                    }
                }
            }
            inputs.isEmpty() -> Column(
                Modifier.width(1000.dp).background(c.sf, R4).border(1.dp, c.ln, R4).padding(horizontal = 48.dp, vertical = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(Modifier.border(1.dp, c.lnS, R4).padding(horizontal = 12.dp, vertical = 2.dp)) { CText("SIN ENTRADAS", 24, color = c.mu, tabular = true) }
                CText("No se encontraron entradas HDMI", 48, weight = FontWeight.SemiBold)
                CText("La tele no reportó sus puertos HDMI. Vuelve a intentarlo en un momento.", 28, color = c.mu)
                CityButton("Volver al inicio", state.base, Modifier.padding(top = 12.dp))
            }
            else -> {
                Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                    inputs.forEachIndexed { i, input -> InputCard(input, focused = state.base && state.input == i) }
                }
                CText("OK cambia de entrada · Izquierda abre el menú · Atrás vuelve al inicio", 24, color = c.mu, tabular = true)
            }
        }
    }
}

@Composable
private fun InputCard(input: TvInput, focused: Boolean) {
    val c = city
    val scale by animateFloatAsState(if (focused) 1.04f else 1f, cityTween(260), label = "entrada")
    Box(Modifier.graphicsLayer { scaleX = scale; scaleY = scale }.focusGlow(focused, c.ac, c.glow)) {
        Column(
            Modifier.size(522.dp, 440.dp).background(if (focused) c.sf2 else c.sf, R4)
                .border(1.dp, if (focused) c.ac else c.ln, R4).padding(36.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(Modifier.width(450.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Box(
                    Modifier.size(132.dp, 60.dp).border(3.dp, c.tx, RoundedCornerShape(10.dp, 10.dp, 24.dp, 24.dp)),
                    contentAlignment = Alignment.Center,
                ) { Box(Modifier.size(80.dp, 14.dp).background(c.mu, RoundedCornerShape(4.dp))) }
                CText("IN-%02d".format(input.port ?: 0), 24, color = c.mu, tabular = true)
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CText(input.name, 60, weight = FontWeight.SemiBold, spacing = -.01f)
                CText(if (input.hasSignal) "Dispositivo conectado" else "Sin dispositivo", 28, color = c.mu)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SignalDot(input.hasSignal)
                CText(if (input.hasSignal) "Con señal" else "Sin señal", 26, color = if (input.hasSignal) c.tx else c.mu, weight = FontWeight.Medium)
            }
        }
    }
}

/** Punto de señal: con señal late 3 veces al abrir Entradas y se queda fijo. */
@Composable
private fun SignalDot(on: Boolean) {
    val c = city
    val pulse = remember { Animatable(1f) }
    LaunchedEffect(on) {
        if (!on) return@LaunchedEffect
        delay(350)
        repeat(3) {
            pulse.snapTo(0f)
            pulse.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
        }
    }
    Box(Modifier.size(20.dp)) {
        if (on) {
            Box(
                Modifier.fillMaxSize()
                    .graphicsLayer { val s = 1f + pulse.value * 1.6f; scaleX = s; scaleY = s; alpha = .9f * (1f - pulse.value) }
                    .border(2.dp, c.ac, CircleShape),
            )
            Box(Modifier.fillMaxSize().background(c.ac, CircleShape))
        } else {
            Box(Modifier.fillMaxSize().border(2.dp, c.mu, CircleShape).background(Color.Transparent))
        }
    }
}
