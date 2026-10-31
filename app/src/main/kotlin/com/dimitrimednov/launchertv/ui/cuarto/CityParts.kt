package com.dimitrimednov.launchertv.ui.cuarto

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.tv.material3.LocalTextStyle
import androidx.tv.material3.Text

val R4 = RoundedCornerShape(4.dp)
val R2 = RoundedCornerShape(2.dp)

/**
 * Texto del diseño. [condensed] = Barlow Condensed (títulos, hora, botones); si no, Barlow.
 * [spacing] va en em, como `letter-spacing` en el CSS.
 */
@Composable
fun CText(
    text: String,
    size: Int,
    modifier: Modifier = Modifier,
    color: Color = city.tx,
    weight: FontWeight = FontWeight.Normal,
    condensed: Boolean = false,
    upper: Boolean = false,
    spacing: Float = 0f,
    tabular: Boolean = false,
    lineHeight: Float? = null,
    maxLines: Int = 1,
) {
    Text(
        text = if (upper) text.uppercase() else text,
        modifier = modifier,
        color = color,
        fontSize = size.sp,
        fontWeight = weight,
        fontFamily = if (condensed) BarlowCondensed else Barlow,
        letterSpacing = spacing.em,
        lineHeight = lineHeight?.let { (size * it).sp } ?: (size * 1.2f).sp,
        maxLines = maxLines,
        softWrap = maxLines > 1,
        overflow = TextOverflow.Ellipsis,
        style = if (tabular) LocalTextStyle.current.merge(TextStyle(fontFeatureSettings = "tnum")) else LocalTextStyle.current,
    )
}

/** Etiqueta gris de sección ("ACCESOS RÁPIDOS"). */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) =
    CText(text, 24, modifier.height(32.dp), color = city.mu, tabular = true)

/**
 * Marco de foco del diseño: borde de 3 px de acento 5 px por fuera y un resplandor suave.
 * El resplandor se dibuja con trazos concéntricos (sin desenfoque, que es caro en la tele).
 */
fun Modifier.focusGlow(focused: Boolean, color: Color, glow: Color, radius: Dp = 4.dp): Modifier = composed {
    val a by animateFloatAsState(if (focused) 1f else 0f, tween(200), label = "foco")
    drawWithCache {
        val r = radius.toPx()
        onDrawWithContent {
            drawContent()
            if (a <= 0f) return@onDrawWithContent
            for (i in 1..4) {
                val out = 5.dp.toPx() + i * 5.dp.toPx()
                drawRoundRect(
                    glow.copy(alpha = glow.alpha * a * (0.55f - i * 0.12f)),
                    topLeft = Offset(-out, -out), size = Size(size.width + out * 2, size.height + out * 2),
                    cornerRadius = CornerRadius(r + out), style = Stroke(5.dp.toPx()),
                )
            }
            val o = 5.dp.toPx() - 1.5.dp.toPx()
            drawRoundRect(
                color.copy(alpha = color.alpha * a),
                topLeft = Offset(-o, -o), size = Size(size.width + o * 2, size.height + o * 2),
                cornerRadius = CornerRadius(r + o), style = Stroke(3.dp.toPx()),
            )
        }
    }
}

/** Botón del diseño (`btn`): con foco se llena de acento, crece, se ladea 1° y gana sombra dura. */
@Composable
fun CityButton(
    label: String,
    focused: Boolean,
    modifier: Modifier = Modifier,
    height: Int = 64,
    fontSize: Int = 26,
    padding: Int = 34,
    selected: Boolean = false,
    content: (@Composable RowScope.() -> Unit)? = null,
) {
    val c = city
    val scale by animateFloatAsState(if (focused) 1.06f else 1f, cityTween(240), label = "botón")
    val shape = R4
    Row(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale; rotationZ = if (focused) -1f else 0f }
            .drawBehind {
                if (focused) {
                    drawRoundRect(Color.Black, topLeft = Offset(5.dp.toPx(), 5.dp.toPx()), size = size, cornerRadius = CornerRadius(4.dp.toPx()))
                }
            }
            .focusGlow(focused, Color.Transparent, c.glow)
            .height(height.dp)
            .background(if (focused) c.ac else c.sf2, shape)
            .border(if (selected && !focused) 2.dp else 1.dp, if (focused) c.ac else if (selected) c.ac2 else c.lnS, shape)
            .padding(horizontal = padding.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
    ) {
        if (content != null) content() else CText(
            label, fontSize, color = if (focused) c.onAc else c.tx, weight = FontWeight.Medium,
            condensed = true, upper = true, spacing = .05f,
        )
    }
}

/**
 * Ventanas iluminadas de los edificios: `repeating-linear-gradient` del diseño (franjas del color
 * de fondo de 10 px cada 22 px y de 16 px cada 28 px). El mosaico se hace una vez y se repite.
 */
@Composable
fun rememberWindowTile(frame: Color, tileW: Int = 22, tileH: Int = 28, solidW: Int = 10, solidH: Int = 16): Brush {
    val px = LocalDensity.current.density
    return remember(frame, px, tileW, tileH) {
        val tile = tileBitmap((tileW * px).toInt(), (tileH * px).toInt()) { canvas, w, h ->
            val paint = Paint().apply { color = frame }
            canvas.drawRect(0f, 0f, solidW * px, h.toFloat(), paint)
            canvas.drawRect(0f, 0f, w.toFloat(), solidH * px, paint)
        }
        ShaderBrush(ImageShader(tile, TileMode.Repeated, TileMode.Repeated))
    }
}

/** Puntos del panel abierto: `radial-gradient(rgba(255,255,255,.07) 1.4px, transparent 1.8px)` cada 12 px. */
@Composable
fun rememberDotTile(): Brush {
    val px = LocalDensity.current.density
    return remember(px) {
        val step = (12 * px).toInt().coerceAtLeast(2)
        val tile = tileBitmap(step, step) { canvas, w, h ->
            canvas.drawCircle(Offset(w / 2f, h / 2f), 1.6f * px, Paint().apply { color = Color.White.copy(alpha = .07f) })
        }
        ShaderBrush(ImageShader(tile, TileMode.Repeated, TileMode.Repeated))
    }
}

/** Color de base con un mosaico encima (ventanas o puntos). */
fun Modifier.tiled(base: Color, tile: Brush): Modifier = drawBehind {
    drawRect(base)
    drawRect(tile)
}

private fun tileBitmap(w: Int, h: Int, draw: (Canvas, Int, Int) -> Unit): ImageBitmap {
    val bitmap = ImageBitmap(w.coerceAtLeast(1), h.coerceAtLeast(1))
    draw(Canvas(bitmap), w, h)
    return bitmap
}

/** Texto girado para leerse de abajo hacia arriba (`writing-mode: vertical-rl` + `rotate(180deg)`). */
fun Modifier.vertical(): Modifier = layout { measurable, _ ->
    val p = measurable.measure(Constraints())
    layout(p.height, p.width) {
        p.placeWithLayer(x = -(p.width - p.height) / 2, y = (p.width - p.height) / 2) { rotationZ = -90f }
    }
}

/** Esqueleto de carga: bloque del color de superficie 2. */
@Composable
fun Bone(w: Int, h: Int, modifier: Modifier = Modifier, radius: Int = 2) =
    Box(modifier.size(w.dp, h.dp).background(city.sf2, RoundedCornerShape(radius.dp)))
