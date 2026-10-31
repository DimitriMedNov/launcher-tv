package com.dimitrimednov.launchertv.ui.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.tv.material3.Text
import com.dimitrimednov.launchertv.data.LauncherApp
import com.dimitrimednov.launchertv.ui.theme.Layout
import com.dimitrimednov.launchertv.ui.theme.Motion
import com.dimitrimednov.launchertv.ui.theme.Palette

/** Medidas de la cuadrícula; cambian cuando la cuadrícula tiene el foco (sube y muestra 3 filas). */
data class GridGeometry(val tileW: Dp, val tileH: Dp, val rowH: Dp, val top: Dp, val startRow: Int, val visibleRows: Int) {
    fun x(index: Int): Dp = (tileW + Layout.GridGap) * (index % Layout.GRID_COLUMNS)
    fun y(index: Int): Dp = rowH * (index / Layout.GRID_COLUMNS - startRow)
}

fun gridGeometry(focused: Boolean, index: Int): GridGeometry {
    val cols = Layout.GRID_COLUMNS
    val tileW = ((Layout.GridWidth.value - Layout.GridGap.value * (cols - 1)) / cols).toInt().dp
    val tileH = Math.round(tileW.value * 9 / 16).dp
    return GridGeometry(
        tileW = tileW,
        tileH = tileH,
        rowH = tileH + Layout.GridGap + if (focused) 36.dp else 0.dp,
        top = if (focused) 612.dp else 742.dp,
        startRow = if (focused) (index / cols - 1).coerceAtLeast(0) else 0,
        visibleRows = if (focused) 3 else 2,
    )
}

/** "Tus apps": cuadrícula de 6 columnas con la fila siguiente asomándose al 32 %. */
@Composable
fun AppGrid(apps: List<LauncherApp>, state: NavState, accent: Color) {
    val focused = state.zone == Zone.Grid && state.panel == null
    val geo = gridGeometry(focused || state.moving != null, state.grid)
    val top by animateDpAsState(geo.top, Motion.ease(480), label = "alto cuadrícula")

    Row(
        Modifier.offsetDp({ Layout.ContentLeft }, { top - 50.dp }),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Tus apps", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        val counter by animateFloatAsState(if (focused) 1f else 0f, tween(250), label = "contador")
        Text(
            "${state.grid + 1} de ${apps.size}", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = Palette.Muted,
            modifier = Modifier.graphicsLayer { alpha = counter },
        )
    }

    Box(Modifier.offsetDp({ Layout.ContentLeft }, { top }).width(Layout.GridWidth)) {
        Spotlight(geo, state.grid, visible = focused, accent = accent)
        apps.forEachIndexed { i, app ->
            key(app.packageName) {
                GridTile(app, i, geo, state, focused)
            }
        }
    }
}

@Composable
private fun GridTile(app: LauncherApp, i: Int, geo: GridGeometry, state: NavState, gridFocused: Boolean) {
    val isFocused = gridFocused && i == state.grid
    val isMoving = state.moving == app.packageName
    val row = i / Layout.GRID_COLUMNS - geo.startRow
    val visible = row in 0 until geo.visibleRows
    val peek = gridFocused && row == geo.visibleRows - 1
    val targetAlpha = when {
        !visible -> 0f
        peek -> .32f
        state.moving != null && !isMoving -> .6f
        gridFocused && !isFocused -> .78f
        else -> 1f
    }
    val alpha by animateFloatAsState(targetAlpha, tween(420), label = "opacidad")
    val x by animateDpAsState(geo.x(i), spring(dampingRatio = .8f, stiffness = 400f), label = "x")
    val y by animateDpAsState(geo.y(i), Motion.ease(480), label = "y")
    val look = when {
        isMoving -> TileLook.Lifted
        isFocused && state.pressed == "tile${app.packageName}" -> TileLook.Pressed
        isFocused -> TileLook.Focused
        else -> TileLook.Rest
    }
    val label by animateFloatAsState(if (isFocused || isMoving) 1f else 0f, tween(220), label = "nombre")

    Box(
        Modifier
            .offsetDp({ x }, { y })
            .zIndex(if (isFocused || isMoving) 2f else 1f)
            .graphicsLayer { this.alpha = alpha },
    ) {
        AppTile(app.name, app.brand, look, state.lastDir, state.tiltTick, geo.tileW, geo.tileH)
        // Nombre en píldora debajo de la tarjeta enfocada (el brief: el nombre solo aparece con foco).
        Box(Modifier.width(geo.tileW).offset(y = geo.tileH + 18.dp), contentAlignment = Alignment.TopCenter) {
            Text(
                app.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1,
                modifier = Modifier
                    .graphicsLayer { this.alpha = label; scaleX = .9f + .1f * label; scaleY = .9f + .1f * label; translationY = (1 - label) * -10f }
                    .background(Palette.glass(.14f), CircleShape)
                    .padding(horizontal = 18.dp, vertical = 8.dp),
            )
        }
    }
}

/** Brillo del color de la app detrás de la tarjeta enfocada; se desliza con rebote. */
@Composable
private fun Spotlight(geo: GridGeometry, index: Int, visible: Boolean, accent: Color) {
    val x by animateDpAsState(geo.x(index) - 80.dp, spring(dampingRatio = .65f, stiffness = 300f), label = "luz x")
    val y by animateDpAsState(geo.y(index) - 70.dp, spring(dampingRatio = .65f, stiffness = 300f), label = "luz y")
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(300), label = "luz")
    Box(
        Modifier
            .offsetDp({ x }, { y })
            .size(geo.tileW + 160.dp, geo.tileH + 140.dp)
            .graphicsLayer { this.alpha = alpha }
            .background(Brush.radialGradient(listOf(accent.copy(alpha = .42f), Color.Transparent)), RoundedCornerShape(120.dp)),
    )
}

/** Esqueleto con la forma de dos filas de tarjetas (estado "Cargando"). */
@Composable
fun GridSkeleton() {
    val geo = gridGeometry(focused = false, index = 0)
    Box(Modifier.offset(Layout.ContentLeft, geo.top - 50.dp)) {
        Box(Modifier.size(160.dp, 26.dp).shimmer(13.dp))
    }
    Box(Modifier.offset(Layout.ContentLeft, geo.top)) {
        repeat(Layout.GRID_COLUMNS * 2) { i ->
            Box(Modifier.offset(geo.x(i), geo.y(i)).size(geo.tileW, geo.tileH).shimmer(Layout.TileRadius, i * 40))
        }
    }
}

/** Estado "Sin apps visibles": mensaje y botón para volver a mostrar las ocultas. */
@Composable
fun NoAppsMessage(focused: Boolean, pressed: Boolean) {
    Column(Modifier.offset(Layout.ContentLeft, 680.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("No hay apps en el inicio", fontSize = 40.sp, fontWeight = FontWeight.Bold, letterSpacing = (-.4).sp)
        Text("Las apps ocultas se pueden mostrar de nuevo.", fontSize = 28.sp, color = Palette.Secondary)
        PillButton("Agregar apps", Icons.Rounded.Add, focused, pressed, Palette.Accent, Modifier.padding(top = 12.dp))
    }
}
