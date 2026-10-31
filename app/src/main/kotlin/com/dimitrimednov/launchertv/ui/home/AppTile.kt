package com.dimitrimednov.launchertv.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.dimitrimednov.launchertv.data.Brand
import com.dimitrimednov.launchertv.ui.theme.Layout
import com.dimitrimednov.launchertv.ui.theme.Motion
import com.dimitrimednov.launchertv.ui.theme.Palette
import kotlin.math.cos
import kotlin.math.sin

private val TileShape = RoundedCornerShape(Layout.TileRadius)

/** Cómo se ve una tarjeta en este momento. */
enum class TileLook { Rest, Focused, Pressed, Lifted }

/**
 * Tarjeta 16:9 de una app: color de la marca y su nombre (o ícono). Enfocada crece al 106 %,
 * gana el anillo blanco con separación, se inclina 7° hacia la flecha y cruza un destello.
 */
@Composable
fun AppTile(
    name: String,
    brand: Brand,
    look: TileLook,
    tiltDir: Dir,
    tiltTick: Int,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    val focused = look == TileLook.Focused || look == TileLook.Lifted
    val scale by animateFloatAsState(
        targetValue = when (look) { TileLook.Rest -> 1f; TileLook.Focused -> 1.06f; TileLook.Pressed -> .96f; TileLook.Lifted -> 1.12f },
        animationSpec = if (look == TileLook.Pressed) tween(120) else Motion.bouncy(),
        label = "escala tarjeta",
    )
    val ring by animateFloatAsState(if (look == TileLook.Focused || look == TileLook.Pressed) 1f else 0f, tween(200), label = "anillo")
    val lift by animateFloatAsState(if (look == TileLook.Lifted) -10f else 0f, Motion.bouncy(), label = "levantar")
    val highlight by animateFloatAsState(if (focused) 1f else .3f, tween(300), label = "reflejo")
    val tilt = remember { Animatable(0f) }
    LaunchedEffect(tiltTick, focused) {
        if (!focused || tiltTick == 0) return@LaunchedEffect
        tilt.snapTo(1f)
        tilt.animateTo(0f, tween(320, easing = Motion.Ease))
    }

    Box(
        modifier
            .size(width, height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = lift
                cameraDistance = 900f * density
                val t = tilt.value * 7f
                when (tiltDir) {
                    Dir.Right -> rotationY = t
                    Dir.Left -> rotationY = -t
                    Dir.Up -> rotationX = t
                    Dir.Down -> rotationX = -t
                }
            }
            .drawBehind {
                if (ring <= 0f) return@drawBehind
                // Anillo del diseño: 4 px del color de fondo y luego 4 px blancos.
                val r = Layout.TileRadius.toPx()
                drawRoundRect(Palette.White.copy(alpha = ring), Offset(-8.dp.toPx(), -8.dp.toPx()),
                    Size(size.width + 16.dp.toPx(), size.height + 16.dp.toPx()), CornerRadius(r + 8.dp.toPx()))
                drawRoundRect(Palette.Bg.copy(alpha = ring), Offset(-4.dp.toPx(), -4.dp.toPx()),
                    Size(size.width + 8.dp.toPx(), size.height + 8.dp.toPx()), CornerRadius(r + 4.dp.toPx()))
            }
            .shadow(
                elevation = if (focused) 36.dp else 8.dp,
                shape = TileShape,
                ambientColor = if (focused) brand.accent else Color.Black,
                spotColor = if (focused) brand.accent else Color.Black,
            )
            .background(brandBrush(brand), TileShape)
            .border(1.dp, Palette.glass(.07f), TileShape)
            .shine(focused && look != TileLook.Pressed, Layout.TileRadius),
        contentAlignment = Alignment.Center,
    ) {
        if (brand.icon != null) {
            Icon(brand.icon, contentDescription = name, tint = brand.foreground, modifier = Modifier.size(60.dp))
        } else {
            Text(
                name, color = brand.foreground, fontWeight = FontWeight.ExtraBold, letterSpacing = (-.5).sp,
                fontSize = if (name.length > 13) 24.sp else 30.sp, lineHeight = 30.sp,
                textAlign = TextAlign.Center, maxLines = 2, modifier = Modifier.padding(horizontal = 14.dp),
            )
        }
        Box(
            Modifier.fillMaxSize().graphicsLayer { alpha = highlight }.background(
                Brush.linearGradient(0f to Palette.glass(.2f), .42f to Color.Transparent, end = Offset(220f, 400f)),
                TileShape,
            ),
        )
    }
}

/** Fondo de la marca: color sólido o degradado con el ángulo CSS del diseño. */
fun brandBrush(brand: Brand): Brush {
    if (brand.background.size == 1) return androidx.compose.ui.graphics.SolidColor(brand.background.first())
    val rad = Math.toRadians(brand.gradientAngle.toDouble() - 90)
    val dx = cos(rad).toFloat()
    val dy = sin(rad).toFloat()
    return Brush.linearGradient(
        brand.background,
        start = Offset(150f - dx * 150f, 85f - dy * 85f),
        end = Offset(150f + dx * 150f, 85f + dy * 85f),
    )
}
