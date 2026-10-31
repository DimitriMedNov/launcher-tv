package com.dimitrimednov.launchertv.ui.home

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.dimitrimednov.launchertv.ui.icons.SymbolIcons
import com.dimitrimednov.launchertv.ui.theme.Motion
import com.dimitrimednov.launchertv.ui.theme.Palette

private val RailShape = RoundedCornerShape(48.dp)

/** Barra lateral "Mi TV": angosta con solo íconos; se abre a 320 dp cuando tiene el foco. */
@Composable
fun Rail(section: Section, open: Boolean, accent: Color) {
    val width by animateDpAsState(if (open) 320.dp else 104.dp, spring(dampingRatio = .72f, stiffness = 380f), label = "ancho barra")
    val bg by animateColorAsState(if (open) Palette.Panel.copy(alpha = .92f) else Palette.glass(.06f), tween(300), label = "fondo barra")
    val labels by animateFloatAsState(if (open) 1f else 0f, tween(220, 60), label = "etiquetas")
    val indicatorTop by animateDpAsState((section.ordinal * 80).dp, spring(dampingRatio = .6f, stiffness = 420f), label = "indicador")
    val indicatorColor by animateColorAsState(if (open) Palette.White else Palette.glass(.14f), tween(250), label = "color indicador")
    val logo by animateColorAsState(accent, tween(500), label = "logo")

    Box(Modifier.fillMaxHeight().offset(x = 32.dp), contentAlignment = Alignment.CenterStart) {
        Column(
            modifier = Modifier
                .width(width)
                .shadow(if (open) 40.dp else 0.dp, RailShape)
                .clip(RailShape)
                .background(bg)
                .border(1.dp, Palette.glass(if (open) .12f else .1f), RailShape)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                Modifier.height(64.dp).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Icon(SymbolIcons.Tv, contentDescription = null, tint = logo, modifier = Modifier.size(40.dp))
                Text(
                    "Mi TV", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1,
                    modifier = Modifier.graphicsLayer { alpha = labels; translationX = (1 - labels) * -12f },
                )
            }
            Box(Modifier.padding(top = 8.dp)) {
                Box(
                    Modifier
                        .offsetDp({ 0.dp }, { indicatorTop })
                        .fillMaxWidth()
                        .height(72.dp)
                        .shadow(if (open) 14.dp else 0.dp, CircleShape)
                        .background(indicatorColor, CircleShape),
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Section.entries.forEach { item -> RailItem(item, current = item == section, open = open, labels = labels) }
                }
            }
        }
    }
}

@Composable
private fun RailItem(item: Section, current: Boolean, open: Boolean, labels: Float) {
    val color by animateColorAsState(
        when {
            open && current -> Palette.Bg
            current -> Palette.White
            else -> Palette.Tertiary
        },
        tween(220), label = "color sección",
    )
    val iconScale by animateFloatAsState(if (open && current) 1.12f else 1f, Motion.bouncy(), label = "ícono sección")
    Row(
        Modifier.height(72.dp).fillMaxWidth().padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Icon(if (current) item.icon else item.outlinedIcon, contentDescription = item.label, tint = color, modifier = Modifier.size(36.dp).scale(iconScale))
        Text(
            item.label, color = color, fontSize = 28.sp, fontWeight = FontWeight.Bold, maxLines = 1,
            modifier = Modifier.graphicsLayer { alpha = labels; translationX = (1 - labels) * -12f },
        )
    }
}
