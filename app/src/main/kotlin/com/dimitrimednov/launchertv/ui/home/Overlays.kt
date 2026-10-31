package com.dimitrimednov.launchertv.ui.home

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.dimitrimednov.launchertv.ui.theme.Layout
import com.dimitrimednov.launchertv.ui.theme.Motion
import com.dimitrimednov.launchertv.ui.theme.Palette
import kotlinx.coroutines.launch

/** Aviso blanco abajo al centro que entra con rebote. */
@Composable
fun Toast(message: String?, accent: androidx.compose.ui.graphics.Color) {
    var last by remember { mutableStateOf("") }
    if (message != null) last = message
    Box(Modifier.fillMaxSize().padding(bottom = 56.dp), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            visible = message != null,
            enter = fadeIn(tween(250)) + slideInVertically(Motion.bouncy()) { 24 } + scaleIn(Motion.bouncy(), .9f),
            exit = fadeOut(tween(250)) + slideOutVertically(tween(250)) { 24 } + scaleOut(tween(250), .9f),
        ) {
            Row(
                Modifier
                    .shadow(30.dp, CircleShape)
                    .background(Palette.White.copy(alpha = .96f), CircleShape)
                    .padding(start = 22.dp, end = 30.dp, top = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // El aviso es blanco: si el acento de la app es claro (YouTube, Play Store…) se usa el azul del diseño.
                val tint = if (accent.luminance() > .6f) Palette.Accent else accent
                Icon(Icons.Rounded.Info, contentDescription = null, tint = tint, modifier = Modifier.size(30.dp))
                Text(last, color = Palette.Bg, fontSize = 24.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}

/**
 * Al abrir una app, su tarjeta crece hasta llenar la pantalla con el color de la marca,
 * el nombre y una barra que se llena; después Android abre la app encima.
 */
@Composable
fun OpenOverlay(opening: Opening?, gridFocused: Boolean) {
    if (opening == null) return
    val app = opening.app
    val grow = remember(opening) { Animatable(0f) }
    val content = remember(opening) { Animatable(0f) }
    LaunchedEffect(opening) {
        launch { grow.animateTo(1f, Motion.ease(560)) }
        content.animateTo(1f, Motion.ease(700, 200))
    }
    val geo = gridGeometry(gridFocused, opening.gridIndex)
    val fromX = if (opening.fromGrid) Layout.ContentLeft + geo.x(opening.gridIndex) else Layout.ContentLeft
    val fromY = if (opening.fromGrid) geo.top + geo.y(opening.gridIndex) else Layout.SectionTop
    val fromW = if (opening.fromGrid) geo.tileW else 1100.dp
    val fromH = if (opening.fromGrid) geo.tileH else 420.dp
    val g = grow.value
    Box(
        Modifier
            .offset(lerp(fromX, 0.dp, g), lerp(fromY, 0.dp, g))
            .size(lerp(fromW, 1920.dp, g), lerp(fromH, 1080.dp, g))
            .clip(RoundedCornerShape(lerp(if (opening.fromGrid) Layout.TileRadius else 40.dp, 0.dp, g)))
            .background(brandBrush(app.brand)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.graphicsLayer {
                alpha = content.value
                val s = .7f + .3f * content.value
                scaleX = s
                scaleY = s
            },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(44.dp),
        ) {
            app.brand.icon?.let { Icon(it, contentDescription = null, tint = app.brand.foreground, modifier = Modifier.size(140.dp)) }
            Text(app.name, color = app.brand.foreground, fontSize = 88.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-2).sp, maxLines = 1)
            Box(Modifier.size(260.dp, 6.dp).clip(CircleShape).background(app.brand.foreground.copy(alpha = .2f))) {
                val bar = remember(opening) { Animatable(0f) }
                LaunchedEffect(opening) { bar.animateTo(1f, tween(1000, 300, androidx.compose.animation.core.CubicBezierEasing(.6f, 0f, .3f, 1f))) }
                Box(Modifier.fillMaxHeight().fillMaxWidth(bar.value).background(app.brand.foreground, CircleShape))
            }
        }
    }
}

/** Animación de arranque: el fondo aparece, luego la barra de arriba y al final las tarjetas. */
@Composable
fun rememberBootStage(): Int {
    var stage by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(80); stage = 1
        kotlinx.coroutines.delay(370); stage = 2
        kotlinx.coroutines.delay(350); stage = 3
    }
    return stage
}
