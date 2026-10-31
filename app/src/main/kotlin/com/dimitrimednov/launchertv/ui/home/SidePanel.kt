package com.dimitrimednov.launchertv.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.dimitrimednov.launchertv.data.InputKind
import com.dimitrimednov.launchertv.data.LauncherApp
import com.dimitrimednov.launchertv.data.TvInput
import com.dimitrimednov.launchertv.ui.icons.InputIcon
import com.dimitrimednov.launchertv.ui.theme.Motion
import com.dimitrimednov.launchertv.ui.theme.Palette

/**
 * Panel lateral derecho de 560 dp: menú al mantener OK, selector de ícono de entrada y apps ocultas.
 * Sin desenfoque (la tele del cuarto no lo soporta): fondo `#1C1C1E` al 94 %.
 */
@Composable
fun SidePanel(panel: Panel?, index: Int, apps: List<LauncherApp>, hidden: List<LauncherApp>, inputs: List<TvInput>) {
    // Se recuerda el último panel para que se siga viendo mientras sale.
    var shown by remember { mutableStateOf(panel) }
    if (panel != null) shown = panel
    AnimatedVisibility(
        visible = panel != null,
        enter = slideInHorizontally(spring(dampingRatio = .82f, stiffness = 420f)) { it } + fadeIn(tween(250)),
        exit = slideOutHorizontally(tween(260, easing = Motion.EaseIn)) { it } + fadeOut(tween(250)),
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
            Column(
                Modifier
                    .width(560.dp)
                    .fillMaxHeight()
                    .background(Palette.Panel.copy(alpha = .94f))
                    .border(1.dp, Palette.glass(.08f))
                    .padding(horizontal = 48.dp, vertical = 54.dp),
                verticalArrangement = Arrangement.spacedBy(36.dp),
            ) {
                when (val current = shown) {
                    is Panel.AppMenu -> apps.firstOrNull { it.packageName == current.packageName }?.let { app ->
                        AppTile(app.name, app.brand, TileLook.Rest, Dir.Right, 0, 240.dp, 135.dp)
                        Title(app.name)
                        MenuList(AppMenuItem.entries.map { it.label to it.icon }, index)
                    }
                    is Panel.InputMenu -> inputs.firstOrNull { it.id == current.inputId }?.let { input ->
                        InputPreview(input)
                        Title(input.name)
                        MenuList(InputMenuItem.entries.map { it.label to it.icon }, index)
                    }
                    is Panel.IconPicker -> inputs.firstOrNull { it.id == current.inputId }?.let { input ->
                        Title("Elegir ícono")
                        Text("Entrada: ${input.port?.let { "HDMI $it" } ?: "Antena"}", fontSize = 28.sp, color = Palette.Tertiary)
                        IconGrid(index, input.port)
                    }
                    Panel.HiddenApps -> {
                        Title("Apps ocultas")
                        if (hidden.isEmpty()) {
                            Text("No hay apps ocultas.", fontSize = 28.sp, color = Palette.Tertiary)
                        } else {
                            Text("OK vuelve a mostrar la app en el inicio.", fontSize = 24.sp, color = Palette.Tertiary)
                            MenuList(hidden.map { it.name to Icons.Rounded.Visibility }, index, maxVisible = 7)
                        }
                    }
                    null -> Unit
                }
            }
        }
    }
}

@Composable
private fun Title(text: String) = Text(text, fontSize = 40.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-.4).sp, maxLines = 1)

@Composable
private fun InputPreview(input: TvInput) {
    Box(
        Modifier.size(240.dp, 135.dp).background(Palette.glass(.08f), RoundedCornerShape(24.dp)).border(1.dp, Palette.glass(.1f), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center,
    ) { InputIcon(input.kind, 64.dp, Palette.White, input.port.takeIf { input.kind == InputKind.Hdmi }) }
}

/** Lista con la píldora blanca que se desliza al elemento elegido (igual que Ajustes rápidos). */
@Composable
private fun MenuList(items: List<Pair<String, ImageVector>>, index: Int, maxVisible: Int = items.size) {
    val first = (index - maxVisible + 1).coerceAtLeast(0)
    val y by animateDpAsState(((index - first) * 82).dp, spring(dampingRatio = .62f, stiffness = 420f), label = "menú")
    Box {
        Box(Modifier.offsetDp({ 0.dp }, { y }).fillMaxWidth().height(76.dp).background(Palette.White, CircleShape))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items.drop(first).take(maxVisible).forEachIndexed { i, (label, icon) ->
                val selected = i + first == index
                val color by animateColorAsState(if (selected) Palette.Bg else Palette.White, tween(200), label = "texto menú")
                Row(
                    Modifier.height(76.dp).fillMaxWidth().padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
                    Text(label, color = color, fontSize = 28.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun IconGrid(index: Int, port: Int?) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InputKind.entries.chunked(2).forEachIndexed { row, pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                pair.forEachIndexed { col, kind ->
                    val selected = row * 2 + col == index
                    val bg by animateColorAsState(if (selected) Palette.White else Palette.glass(.07f), tween(200), label = "fondo ícono")
                    val fg by animateColorAsState(if (selected) Palette.Bg else Palette.White, tween(200), label = "color ícono")
                    val scale by androidx.compose.animation.core.animateFloatAsState(if (selected) 1.04f else 1f, Motion.bouncy(), label = "escala ícono")
                    Column(
                        Modifier.size(224.dp, 112.dp).scale(scale).background(bg, RoundedCornerShape(20.dp)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                    ) {
                        InputIcon(kind, 44.dp, fg, port.takeIf { kind == InputKind.Hdmi })
                        Text(kind.label, color = fg, fontSize = 24.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                    }
                }
            }
        }
    }
}
