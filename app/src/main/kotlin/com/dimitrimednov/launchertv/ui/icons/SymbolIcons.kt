package com.dimitrimednov.launchertv.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Íconos rellenos como los "Material Symbols Rounded" con `FILL 1` del diseño.
 * Material Icons solo trae versiones con antena o en contorno, así que estos se dibujan aquí.
 */
object SymbolIcons {

    /** `tv` relleno: el logo "Mi TV" de la barra lateral. */
    val Tv: ImageVector by lazy {
        ImageVector.Builder("tv", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color.White)) {
                // Pantalla con esquinas redondeadas.
                moveTo(5f, 4f); horizontalLineTo(19f)
                arcTo(2.5f, 2.5f, 0f, false, true, 21.5f, 6.5f); verticalLineTo(15.5f)
                arcTo(2.5f, 2.5f, 0f, false, true, 19f, 18f); horizontalLineTo(5f)
                arcTo(2.5f, 2.5f, 0f, false, true, 2.5f, 15.5f); verticalLineTo(6.5f)
                arcTo(2.5f, 2.5f, 0f, false, true, 5f, 4f); close()
                // Base.
                moveTo(8.5f, 19.5f); horizontalLineTo(15.5f)
                arcTo(.75f, .75f, 0f, false, true, 15.5f, 21f); horizontalLineTo(8.5f)
                arcTo(.75f, .75f, 0f, false, true, 8.5f, 19.5f); close()
            }
        }.build()
    }

    /** `live_tv` relleno: pantalla con el triángulo de reproducir recortado (tarjeta de Live TV). */
    val LiveTv: ImageVector by lazy {
        ImageVector.Builder("live_tv", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color.White), pathFillType = PathFillType.EvenOdd) {
                moveTo(5f, 4f); horizontalLineTo(19f)
                arcTo(2.5f, 2.5f, 0f, false, true, 21.5f, 6.5f); verticalLineTo(15.5f)
                arcTo(2.5f, 2.5f, 0f, false, true, 19f, 18f); horizontalLineTo(5f)
                arcTo(2.5f, 2.5f, 0f, false, true, 2.5f, 15.5f); verticalLineTo(6.5f)
                arcTo(2.5f, 2.5f, 0f, false, true, 5f, 4f); close()
                moveTo(10f, 8f); lineTo(15.5f, 11f); lineTo(10f, 14f); close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(8.5f, 19.5f); horizontalLineTo(15.5f)
                arcTo(.75f, .75f, 0f, false, true, 15.5f, 21f); horizontalLineTo(8.5f)
                arcTo(.75f, .75f, 0f, false, true, 8.5f, 19.5f); close()
            }
        }.build()
    }
}
