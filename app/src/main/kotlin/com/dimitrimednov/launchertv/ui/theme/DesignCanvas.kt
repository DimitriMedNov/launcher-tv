package com.dimitrimednov.launchertv.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ProvideTextStyle

private val DesignTextStyle = TextStyle(
    fontFamily = Manrope,
    color = Palette.White,
    letterSpacing = 0.sp,
    lineHeight = TextUnit.Unspecified,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
)

private const val DESIGN_WIDTH = 1920f
private const val DESIGN_HEIGHT = 1080f

/**
 * Lienzo de 1920×1080 en el que 1 dp y 1 sp equivalen a 1 px del diseño de Claude Design.
 * Así las medidas se copian tal cual del HTML, sin importar la densidad de la tele.
 */
@Composable
fun DesignCanvas(fontFamily: FontFamily = Manrope, content: @Composable () -> Unit) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(Palette.Bg),
        contentAlignment = Alignment.Center,
    ) {
        val scale = minOf(constraints.maxWidth / DESIGN_WIDTH, constraints.maxHeight / DESIGN_HEIGHT)
        CompositionLocalProvider(LocalDensity provides Density(density = scale, fontScale = 1f)) {
            // Estilo base igual al CSS del diseño: sin el interlineado ni la separación de letras
            // que trae tv-material, y con el interlineado centrado como en el navegador.
            ProvideTextStyle(DesignTextStyle.copy(fontFamily = fontFamily)) {
                Box(Modifier.requiredSize(DESIGN_WIDTH.dp, DESIGN_HEIGHT.dp)) { content() }
            }
        }
    }
}
