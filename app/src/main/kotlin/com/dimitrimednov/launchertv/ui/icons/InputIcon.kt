package com.dimitrimednov.launchertv.ui.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.dimitrimednov.launchertv.data.InputKind
import com.dimitrimednov.launchertv.ui.theme.Manrope

private fun path(d: String) = PathParser().parsePathString(d).toPath()

private val ConsoleBody = path("M18 22 H46 A10 10 0 0 1 56 32 L54 44 A6 6 0 0 1 43 46 L40 40 H24 L21 46 A6 6 0 0 1 10 44 L8 32 A10 10 0 0 1 18 22 Z")
private val HdmiPlug = path("M12 24 H52 V36 L46 42 H18 L12 36 Z")
private val AntennaWave1 = path("M49.5 7.9 A7 7 0 0 1 52.1 17.5")
private val AntennaWave2 = path("M52 3.6 A12 12 0 0 1 56.4 20")

/** Ícono de línea del aparato conectado (64×64 como en `icons.js`). En HDMI muestra el número de puerto. */
@Composable
fun InputIcon(kind: InputKind, size: Dp, color: Color, port: Int? = null) {
    val measurer = rememberTextMeasurer()
    Canvas(Modifier.size(size)) {
        scale(this.size.width / 64f, pivot = Offset.Zero) { drawInput(kind, color, port, measurer) }
    }
}

private fun DrawScope.drawInput(kind: InputKind, c: Color, port: Int?, measurer: TextMeasurer) {
    val st = Stroke(3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    fun line(x1: Float, y1: Float, x2: Float, y2: Float) = drawLine(c, Offset(x1, y1), Offset(x2, y2), 3.5f, StrokeCap.Round)
    fun rect(x: Float, y: Float, w: Float, h: Float, r: Float) =
        drawRoundRect(c, Offset(x, y), Size(w, h), CornerRadius(r), style = st)
    fun dot(x: Float, y: Float, r: Float) = drawCircle(c, r, Offset(x, y))

    when (kind) {
        InputKind.Console -> {
            drawPath(ConsoleBody, c, style = st)
            line(19f, 32f, 29f, 32f); line(24f, 27f, 24f, 37f)
            dot(41f, 29f, 2.4f); dot(47f, 34f, 2.4f)
        }
        InputKind.SetTopBox -> {
            rect(10f, 32f, 44f, 18f, 4f)
            line(24f, 32f, 18f, 16f); line(40f, 32f, 46f, 16f); line(17f, 41f, 32f, 41f)
            dot(46f, 41f, 2.4f)
        }
        InputKind.Laptop -> {
            rect(14f, 16f, 36f, 24f, 3f)
            drawPath(Path().apply { moveTo(8f, 46f); lineTo(56f, 46f); lineTo(52f, 51f); lineTo(12f, 51f); close() }, c)
        }
        InputKind.Soundbar -> {
            rect(6f, 26f, 52f, 14f, 7f)
            listOf(18f, 26f, 38f, 46f).forEach { dot(it, 33f, 2.2f) }
        }
        InputKind.Player -> {
            rect(8f, 22f, 48f, 22f, 4f)
            drawCircle(c, 6f, Offset(24f, 33f), style = st); dot(24f, 33f, 1.6f)
            line(36f, 30f, 48f, 30f); line(36f, 36f, 44f, 36f)
        }
        InputKind.Hdmi -> {
            // Mismo `translate(32 20) scale(.7) translate(-32 -32)` del SVG original.
            withTransform({
                translate(32f, 20f)
                scale(.7f, .7f, pivot = Offset.Zero)
                translate(-32f, -32f)
            }) {
                drawPath(HdmiPlug, c, style = st)
                listOf(22f, 28f, 34f, 40f).forEach { x -> drawLine(c, Offset(x, 30f), Offset(x, 34f), 3.5f, StrokeCap.Round) }
            }
            if (port != null) {
                val text = measurer.measure(
                    port.toString(),
                    TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = c),
                )
                drawText(text, topLeft = Offset(32f - text.size.width / 2f, 58f - text.size.height * .78f))
            }
        }
        InputKind.Antenna -> {
            rect(20f, 36f, 24f, 18f, 3f)
            line(28f, 36f, 16f, 16f); line(36f, 36f, 46f, 16f)
            drawPath(AntennaWave1, c, style = Stroke(2.8f, cap = StrokeCap.Round))
            drawPath(AntennaWave2, c.copy(alpha = c.alpha * .6f), style = Stroke(2.8f, cap = StrokeCap.Round))
        }
    }
}
