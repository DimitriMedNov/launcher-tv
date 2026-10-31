package com.dimitrimednov.launchertv.ui.home

import kotlin.math.roundToInt
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.dimitrimednov.launchertv.ui.theme.Motion
import com.dimitrimednov.launchertv.ui.theme.Palette
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

val SpanishMx: Locale = Locale.forLanguageTag("es-MX")

/** Hora actual; se actualiza al cambiar el minuto. */
@Composable
fun rememberNow(): LocalDateTime {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L - (System.currentTimeMillis() % 60_000L))
            now = LocalDateTime.now()
        }
    }
    return now
}

/** "4:12 p. m." como pide el brief. */
fun formatClock(t: LocalDateTime, withPeriod: Boolean = true): String {
    val h = t.hour % 12
    val base = "${if (h == 0) 12 else h}:${"%02d".format(t.minute)}"
    return if (withPeriod) "$base ${if (t.hour < 12) "a. m." else "p. m."}" else base
}

/** "Jueves, 30 de octubre". */
fun formatLongDate(t: LocalDateTime): String =
    t.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", SpanishMx)).replaceFirstChar { it.titlecase(SpanishMx) }

/**
 * Botón redondo del diseño (Ver canal, Más info, Agregar apps…): blanco con texto oscuro cuando
 * tiene el foco, cristal con borde fino cuando no.
 */
@Composable
fun PillButton(
    label: String,
    icon: ImageVector?,
    focused: Boolean,
    pressed: Boolean,
    glow: Color,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = when { pressed -> .95f; focused -> 1.07f; else -> 1f },
        animationSpec = if (pressed) tween(120) else Motion.bouncy(),
        label = "escala botón",
    )
    val bg by animateColorAsState(if (focused) Palette.White else Palette.glass(.12f), tween(220), label = "fondo botón")
    val fg by animateColorAsState(if (focused) Palette.Bg else Palette.White, tween(220), label = "texto botón")
    Row(
        modifier = modifier
            .scale(scale)
            .shadow(if (focused) 28.dp else 0.dp, CircleShape, ambientColor = glow, spotColor = glow)
            .background(bg, CircleShape)
            .border(1.dp, if (focused) Color.Transparent else Palette.glass(.14f), CircleShape)
            .shine(focused && !pressed, 999.dp, peak = .7f)
            .padding(horizontal = 40.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(34.dp))
        Text(label, color = fg, fontSize = 28.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

/** Alto de un renglón "normal" de Manrope respecto al tamaño de letra (ascendente + descendente). */
private const val MANROPE_NORMAL_LINE = 1.366f

/**
 * Imita el `line-height` menor a "normal" del CSS (títulos grandes del diseño: `.98`, `1`, `.9`).
 * El navegador deja que las letras se salgan de su renglón; Compose no lo hace con un solo renglón,
 * así que aquí se recorta la mitad de la diferencia arriba y abajo.
 */
fun Modifier.cssLineHeight(fontSize: TextUnit, lineHeight: TextUnit): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val trim = ((fontSize.toPx() * MANROPE_NORMAL_LINE - lineHeight.toPx()) / 2).roundToInt().coerceAtLeast(0)
    layout(placeable.width, (placeable.height - trim * 2).coerceAtLeast(0)) { placeable.place(0, -trim) }
}

/** Posición animada leída en la fase de dibujo: mueve sin recomponer en cada cuadro. */
fun Modifier.offsetDp(x: () -> Dp, y: () -> Dp): Modifier = offset { IntOffset(x().roundToPx(), y().roundToPx()) }

/** Destello que cruza el elemento una vez cuando gana el foco (el `specular` del diseño). */
fun Modifier.shine(active: Boolean, radius: Dp, peak: Float = .32f): Modifier = composed {
    var progress by remember { mutableFloatStateOf(-1f) }
    LaunchedEffect(active) {
        if (!active) { progress = -1f; return@LaunchedEffect }
        delay(80)
        val start = System.nanoTime()
        while (true) {
            val p = ((System.nanoTime() - start) / 1e6f / 780f).coerceAtMost(1f)
            progress = Motion.Ease.transform(p)
            if (p >= 1f) break
            delay(16)
        }
        progress = -1f
    }
    drawWithContent {
        drawContent()
        if (progress < 0f) return@drawWithContent
        val w = size.width
        val x = -w * 1.2f + progress * w * 2.4f
        drawRoundRect(
            brush = Brush.linearGradient(
                0.35f to Color.Transparent, 0.5f to Color.White.copy(alpha = peak), 0.65f to Color.Transparent,
                start = Offset(x, 0f), end = Offset(x + w * .9f, size.height * .4f),
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius.toPx()),
        )
    }
}

/** Esqueleto de carga con brillo que avanza (estado "Cargando"). */
fun Modifier.shimmer(radius: Dp, delayMs: Int = 0): Modifier = composed {
    val t = rememberInfiniteTransition(label = "esqueleto")
    val x by t.animateFloat(
        initialValue = 1.5f, targetValue = -1.5f,
        animationSpec = infiniteRepeatable(tween(1800, delayMs, LinearEasing)),
        label = "brillo esqueleto",
    )
    background(
        Brush.horizontalGradient(
            0f to Palette.glass(.06f), .5f to Palette.glass(.13f), 1f to Palette.glass(.06f),
            startX = x * 600f, endX = x * 600f + 900f,
        ),
        RoundedCornerShape(radius),
    )
}
