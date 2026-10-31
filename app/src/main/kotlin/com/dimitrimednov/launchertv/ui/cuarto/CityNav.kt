package com.dimitrimednov.launchertv.ui.cuarto

import com.dimitrimednov.launchertv.ui.home.Dir

enum class CityScreen(val label: String, val sub: String) {
    Home("Inicio", "Apps y lo que estás viendo"),
    Inputs("Entradas", "HDMI 1, 2 y 3"),
    Settings("Ajustes", "Temporizador y modo noche"),
}

/** Dónde está el foco dentro del inicio. */
enum class HomeZone { Grid, Panel, Top }

/** Opciones del menú radial; la flecha con la que se elige cada una. */
enum class RadialOption(val label: String, val arrow: String, val dir: Dir) {
    Open("Abrir", "↑", Dir.Up),
    Move("Mover", "→", Dir.Right),
    Hide("Ocultar", "↓", Dir.Down),
    Info("Información", "←", Dir.Left),
}

enum class NightMode(val label: String, val sub: String, val short: String) {
    Auto("Automático", "10 p. m.–7 a. m.", "auto"),
    Always("Siempre", "Todo el día", "fijo"),
    Never("Nunca", "Solo modo día", "fijo"),
}

enum class QuickSetting(val label: String) { Wifi("Wifi"), Picture("Imagen"), Sound("Sonido"), Android("Ajustes de Android") }

/** Minutos del temporizador para dormir; 0 = apagado. */
val TimerOptions = listOf(0, 15, 30, 60, 90)

/** Filas de Ajustes, en el orden en que se recorren con arriba y abajo. */
enum class SettingsRow { Quick, Timer, Night, Forecast, Hidden }

data class Radial(val packageName: String, val option: RadialOption = RadialOption.Open, val ring: Boolean = false)

/** Ventana de información de una app: Abrir, Ajustes de la app o Cerrar. */
data class InfoBox(val packageName: String, val button: Int = 0)

data class CityState(
    val screen: CityScreen = CityScreen.Home,
    val side: Boolean = false,
    val sideIndex: Int = 0,
    val zone: HomeZone = HomeZone.Grid,
    val grid: Int = 0,
    val panelButton: Int = 0,
    val radial: Radial? = null,
    /** Orden antes de empezar a mover, para poder cancelar; null si no se está moviendo. */
    val moveSnapshot: List<String>? = null,
    /** Orden provisional mientras se mueve. */
    val moveOrder: List<String>? = null,
    val info: InfoBox? = null,
    val input: Int = 0,
    val settingsRow: Int = 0,
    val settingsCol: Int = 0,
    val saver: Boolean = false,
) {
    val moving: Boolean get() = moveOrder != null
    /** Sin capas encima: el foco está en la pantalla. */
    val base: Boolean get() = !side && radial == null && info == null && !saver
}

/** Lo que la navegación necesita saber de los datos. */
data class CityData(
    val ready: Boolean = false,
    val apps: List<String> = emptyList(),
    val hidden: List<String> = emptyList(),
    val inputs: Int = 0,
    val offline: Boolean = false,
    val weatherError: Boolean = false,
    val forecastError: Boolean = false,
) {
    /** Botón de arriba del inicio: wifi si no hay internet, reintentar si falló el clima. */
    val topItem: TopItem? get() = when {
        offline -> TopItem.Wifi
        weatherError -> TopItem.WeatherRetry
        else -> null
    }

    val settingsRows: List<SettingsRow> get() = buildList {
        add(SettingsRow.Quick); add(SettingsRow.Timer); add(SettingsRow.Night)
        if (forecastError) add(SettingsRow.Forecast)
        add(SettingsRow.Hidden)
    }

    fun columns(row: SettingsRow) = when (row) {
        SettingsRow.Quick -> QuickSetting.entries.size
        SettingsRow.Timer -> TimerOptions.size
        SettingsRow.Night -> NightMode.entries.size
        SettingsRow.Forecast -> 1
        SettingsRow.Hidden -> maxOf(1, hidden.size)
    }
}

enum class TopItem { Wifi, WeatherRetry }

/** Botones del panel de la app abierta. */
enum class PanelButton(val label: String) { Open("Abrir"), Info("Más info") }

/** Botones de la ventana de información. */
val InfoButtons = listOf("Abrir", "Ajustes de la app", "Cerrar")
