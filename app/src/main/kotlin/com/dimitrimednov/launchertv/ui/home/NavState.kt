package com.dimitrimednov.launchertv.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Input
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.automirrored.rounded.Input
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.ui.graphics.vector.ImageVector

/** Dónde está el foco. */
enum class Zone { Rail, Hero, Grid, Inputs, Settings }

enum class Dir { Up, Down, Left, Right }

/** Secciones de la barra lateral, en orden. */
enum class Section(val label: String, val icon: ImageVector, val outlinedIcon: ImageVector, val zone: Zone) {
    Inicio("Inicio", Icons.Rounded.Home, Icons.Outlined.Home, Zone.Hero),
    Entradas("Entradas", Icons.AutoMirrored.Rounded.Input, Icons.AutoMirrored.Outlined.Input, Zone.Inputs),
    Ajustes("Ajustes", Icons.Rounded.Tune, Icons.Outlined.Tune, Zone.Settings),
}

enum class SettingItem(val label: String, val icon: ImageVector) {
    Wifi("Wifi", Icons.Rounded.Wifi),
    Picture("Imagen", Icons.Rounded.Image),
    Sound("Sonido", Icons.AutoMirrored.Rounded.VolumeUp),
    Timer("Temporizador para apagar", Icons.Rounded.Timer),
    HiddenApps("Apps ocultas", Icons.Rounded.VisibilityOff),
    Android("Ajustes de Android", Icons.Rounded.Android),
}

enum class AppMenuItem(val label: String, val icon: ImageVector) {
    Open("Abrir", Icons.AutoMirrored.Rounded.OpenInNew),
    Move("Mover", Icons.Rounded.SwapHoriz),
    Hide("Ocultar", Icons.Rounded.VisibilityOff),
    Info("Información de la app", Icons.Rounded.Info),
}

enum class InputMenuItem(val label: String, val icon: ImageVector) {
    Open("Ver entrada", Icons.AutoMirrored.Rounded.Input),
    Icon("Elegir ícono", Icons.Rounded.Edit),
    Info("Información de la entrada", Icons.Rounded.Info),
}

/** Panel lateral derecho abierto encima del inicio. */
sealed interface Panel {
    data class AppMenu(val packageName: String) : Panel
    data class InputMenu(val inputId: String) : Panel
    data class IconPicker(val inputId: String) : Panel
    data object HiddenApps : Panel
}

/** Estado de navegación. Inmutable: cada tecla produce uno nuevo. */
data class NavState(
    val zone: Zone = Zone.Grid,
    val section: Section = Section.Inicio,
    val heroButton: Int = 0,
    val grid: Int = 0,
    val input: Int = 0,
    val setting: Int = 0,
    val panel: Panel? = null,
    val panelIndex: Int = 0,
    /** Orden provisional mientras se mueve una app; null si no se está moviendo. */
    val moveOrder: List<String>? = null,
    val moving: String? = null,
    val lastDir: Dir = Dir.Right,
    /** Sube con cada movimiento para disparar la inclinación de la tarjeta enfocada. */
    val tiltTick: Int = 0,
    /** Elemento que se ve presionado durante 130 ms al dar OK. */
    val pressed: String? = null,
    val saver: Boolean = false,
)

/** Lo que el navegador necesita saber de los datos para no salirse de las listas. */
data class NavData(
    val apps: List<String> = emptyList(),
    val hidden: List<String> = emptyList(),
    val inputs: List<String> = emptyList(),
    val heroButtons: Int = 2,
    val ready: Boolean = false,
)
