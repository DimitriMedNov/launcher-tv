package com.dimitrimednov.launchertv.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.dimitrimednov.launchertv.data.InputKind
import com.dimitrimednov.launchertv.data.TvInput
import com.dimitrimednov.launchertv.ui.icons.InputIcon
import com.dimitrimednov.launchertv.ui.theme.Layout
import com.dimitrimednov.launchertv.ui.theme.Motion
import com.dimitrimednov.launchertv.ui.theme.Palette
import com.dimitrimednov.launchertv.ui.theme.rememberSettlingLoop

private val CardShape = RoundedCornerShape(36.dp)

/** Sección Entradas: nombre grande de la entrada enfocada y tarjetas de 300×300. */
@Composable
fun InputsSection(inputs: List<TvInput>, state: NavState) {
    val focusedInput = inputs.getOrNull(state.input)
    Column(Modifier.offset(Layout.ContentLeft, Layout.SectionTop), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Entradas", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Palette.Label)
        AnimatedContent(
            targetState = focusedInput,
            contentKey = { it?.id },
            transitionSpec = {
                val sign = if (state.lastDir == Dir.Left) -1 else 1
                (fadeIn(tween(380)) + slideInHorizontally(Motion.ease(560)) { sign * 36 }) togetherWith
                    (fadeOut(tween(120)) + slideOutHorizontally(tween(130)) { -sign * 20 })
            },
            label = "entrada enfocada",
        ) { input ->
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    input?.name ?: "Sin entradas", fontSize = 96.sp, fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-3).sp, lineHeight = 96.sp, maxLines = 1,
                    modifier = Modifier.cssLineHeight(96.sp, 96.sp),
                )
                Text(input?.let(::describe) ?: "Esta tele no reporta entradas HDMI", fontSize = 28.sp, color = Palette.Secondary)
            }
        }
    }
    Row(Modifier.offset(Layout.ContentLeft, 520.dp), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
        inputs.forEachIndexed { i, input ->
            val focused = state.zone == Zone.Inputs && state.panel == null && i == state.input
            InputCard(input, focused, pressed = focused && state.pressed == "input${input.id}", state.lastDir, state.tiltTick, i)
        }
    }
}

private fun describe(input: TvInput): String = when {
    input.port == null -> "Antena · Televisión abierta"
    input.hasSignal -> "HDMI ${input.port} · Con señal"
    else -> "Conecta un dispositivo para usar esta entrada"
}

@Composable
private fun InputCard(input: TvInput, focused: Boolean, pressed: Boolean, dir: Dir, tiltTick: Int, index: Int) {
    val scale by animateFloatAsState(
        when { pressed -> .96f; focused -> 1.06f; else -> 1f },
        if (pressed) tween(120) else Motion.bouncy(), label = "escala entrada",
    )
    val lift by animateFloatAsState(if (focused) -8f else 0f, Motion.bouncy(), label = "subir entrada")
    val bg by animateColorAsState(if (focused) Palette.White else Palette.glass(.08f), tween(220), label = "fondo entrada")
    val fg by animateColorAsState(if (focused) Palette.Bg else Palette.White, tween(220), label = "texto entrada")
    val sub by animateColorAsState(if (focused) Palette.OnWhiteMuted else Palette.Tertiary, tween(250), label = "estado entrada")
    val iconLift by animateFloatAsState(if (focused) 1f else 0f, Motion.bouncy(), label = "ícono entrada")
    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) { enter.animateTo(1f, Motion.ease(620, 120 + index * 60)) }
    val tilt = remember { Animatable(0f) }
    LaunchedEffect(tiltTick, focused) {
        if (!focused || tiltTick == 0) return@LaunchedEffect
        tilt.snapTo(1f)
        tilt.animateTo(0f, tween(320, easing = Motion.Ease))
    }

    Column(
        Modifier
            .size(300.dp)
            .graphicsLayer {
                alpha = enter.value
                scaleX = scale
                scaleY = scale
                translationY = lift + (1 - enter.value) * 30f
                cameraDistance = 900f * density
                rotationY = when (dir) { Dir.Right -> 7f; Dir.Left -> -7f; else -> 0f } * tilt.value
            }
            .shadow(if (focused) 32.dp else 0.dp, CardShape)
            .background(bg, CardShape)
            .border(1.dp, if (focused) Palette.White else Palette.glass(.08f), CardShape)
            .shine(focused && !pressed, 36.dp, .6f)
            .padding(32.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            Modifier.graphicsLayer {
                alpha = if (input.hasSignal || input.port == null) 1f else .45f
                translationY = -6f * iconLift
                scaleX = 1f + .08f * iconLift
                scaleY = 1f + .08f * iconLift
            },
        ) {
            InputIcon(input.kind, 72.dp, fg, port = input.port.takeIf { input.kind == InputKind.Hdmi })
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(input.name, color = fg, fontSize = 30.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                SignalDot(input.hasSignal || input.port == null)
                Text(if (input.hasSignal || input.port == null) "Con señal" else "Sin señal", color = sub, fontSize = 24.sp)
            }
        }
    }
}

/** Punto verde que late unas veces cuando hay señal; gris cuando no. */
@Composable
private fun SignalDot(on: Boolean) {
    val ring by rememberSettlingLoop(2000, 0f, 1f, rest = 1f, key = on, cycles = 2, reverse = false)
    Box(
        Modifier
            .size(12.dp)
            .drawBehind {
                if (on) drawCircle(Palette.Signal.copy(alpha = .6f * (1 - ring)), radius = size.minDimension / 2 + 14.dp.toPx() * ring)
            }
            .background(if (on) Palette.Signal else Palette.NoSignal, CircleShape),
    )
}
