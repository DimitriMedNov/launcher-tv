package com.dimitrimednov.launchertv.ui.cuarto

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dimitrimednov.launchertv.ui.home.Dir
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Lo que pasa fuera de la navegación cuando se elige algo. */
interface CityActions {
    fun openApp(packageName: String)
    fun appSettings(packageName: String)
    fun hideApp(packageName: String)
    fun showApp(packageName: String)
    fun saveOrder(packages: List<String>)
    fun openInput(index: Int)
    fun quick(item: QuickSetting)
    fun setTimer(minutes: Int)
    fun setNightMode(mode: NightMode)
    fun retryWeather()
    fun toast(message: String)
}

/**
 * Máquina de estados del control remoto, traducida de `nav`, `ok`, `back` y `onHold` del diseño
 * (`Launcher Cuarto v6.dc.html`).
 */
class CityNavigator(private val scope: CoroutineScope, private val actions: CityActions) {
    var state by mutableStateOf(CityState())
        private set
    var data = CityData()
    private var ringJob: Job? = null

    fun onDirection(dir: Dir) {
        val s = state
        if (s.saver) return closeSaver()
        s.info?.let { info ->
            val next = info.button + if (dir == Dir.Left) -1 else if (dir == Dir.Right) 1 else 0
            if (next in InfoButtons.indices) set { copy(info = info.copy(button = next)) }
            return
        }
        s.radial?.let { r ->
            if (!r.ring) set { copy(radial = r.copy(option = RadialOption.entries.first { it.dir == dir })) }
            return
        }
        if (s.side) {
            when (dir) {
                Dir.Up -> set { copy(sideIndex = (sideIndex - 1).coerceAtLeast(0)) }
                Dir.Down -> set { copy(sideIndex = (sideIndex + 1).coerceAtMost(CityScreen.entries.size - 1)) }
                Dir.Right -> set { copy(side = false) }
                Dir.Left -> Unit
            }
            return
        }
        when (s.screen) {
            CityScreen.Home -> homeDirection(dir)
            CityScreen.Inputs -> when {
                data.inputs == 0 -> if (dir == Dir.Left) openSide()
                dir == Dir.Left -> if (s.input > 0) set { copy(input = input - 1) } else openSide()
                dir == Dir.Right -> if (s.input < data.inputs - 1) set { copy(input = input + 1) }
            }
            CityScreen.Settings -> settingsDirection(dir)
        }
    }

    fun onOk() {
        val s = state
        if (s.saver) return closeSaver()
        s.info?.let { info ->
            set { copy(info = null) }
            when (info.button) {
                0 -> actions.openApp(info.packageName)
                1 -> actions.appSettings(info.packageName)
            }
            return
        }
        if (s.side) return go(CityScreen.entries[s.sideIndex])
        when (s.screen) {
            CityScreen.Home -> homeOk()
            CityScreen.Inputs -> if (data.inputs == 0) go(CityScreen.Home) else actions.openInput(s.input)
            CityScreen.Settings -> settingsOk()
        }
    }

    /** OK mantenido: menú radial sobre la app enfocada. */
    fun onHold() {
        val s = state
        if (s.screen == CityScreen.Home && homeZone() == HomeZone.Grid && !s.moving && s.base) {
            data.apps.getOrNull(s.grid)?.let { set { copy(radial = Radial(it)) } }
        } else {
            onOk()
        }
    }

    /** En el menú radial, Ocultar se confirma manteniendo OK hasta que el anillo se llena. */
    fun wantsRing(): Boolean = state.radial?.option == RadialOption.Hide && !state.saver

    fun startRing() {
        val r = state.radial ?: return
        set { copy(radial = r.copy(ring = true)) }
        ringJob?.cancel()
        ringJob = scope.launch {
            delay(RING_MS)
            ringJob = null
            set { copy(radial = null) }
            hide(r.packageName)
        }
    }

    fun cancelRing() {
        val job = ringJob ?: return
        job.cancel()
        ringJob = null
        state.radial?.let { r -> set { copy(radial = r.copy(ring = false)) } }
        actions.toast("Mantén OK hasta que el anillo se llene")
    }

    fun onBack() {
        val s = state
        when {
            s.saver -> closeSaver()
            s.info != null -> set { copy(info = null) }
            s.radial != null -> { ringJob?.cancel(); ringJob = null; set { copy(radial = null) } }
            s.moving -> cancelMove()
            s.side -> set { copy(side = false) }
            s.screen != CityScreen.Home -> go(CityScreen.Home)
            s.zone != HomeZone.Grid -> set { copy(zone = HomeZone.Grid) }
        }
    }

    /** Botón Home estando ya en el launcher: vuelve al principio. */
    fun reset() {
        ringJob?.cancel()
        state = CityState()
    }

    fun openSaver() = set { copy(saver = true, side = false, radial = null, info = null) }

    fun closeSaver() = set { copy(saver = false) }

    /** Ajusta los índices cuando cambian las listas (app desinstalada, entrada nueva…). */
    fun clamp() = set {
        copy(
            grid = grid.coerceIn(0, (data.apps.size - 1).coerceAtLeast(0)),
            input = input.coerceIn(0, (data.inputs - 1).coerceAtLeast(0)),
            settingsRow = settingsRow.coerceIn(0, data.settingsRows.size - 1),
        )
    }

    /** Zona efectiva: si la zona guardada ya no tiene botones, el foco vuelve a la cuadrícula. */
    fun homeZone(): HomeZone? {
        val s = state
        if (s.zone == HomeZone.Top && data.topItem != null) return HomeZone.Top
        if (!data.ready || data.apps.isEmpty()) return null
        if (s.zone == HomeZone.Panel) return HomeZone.Panel
        return HomeZone.Grid
    }

    /** Fila y columna de Ajustes ya acotadas a lo que existe. */
    fun settingsFocus(): Pair<SettingsRow, Int> {
        val rows = data.settingsRows
        val row = rows[state.settingsRow.coerceIn(0, rows.size - 1)]
        return row to state.settingsCol.coerceIn(0, data.columns(row) - 1)
    }

    private fun homeDirection(dir: Dir) {
        val s = state
        val n = data.apps.size
        if (s.moving) {
            val order = s.moveOrder ?: return
            val target = s.grid + when (dir) { Dir.Left -> -1; Dir.Right -> 1; else -> 0 }
            if (target == s.grid || target !in order.indices) return
            val next = order.toMutableList().apply { add(target, removeAt(s.grid)) }
            return set { copy(moveOrder = next, grid = target) }
        }
        when (homeZone()) {
            HomeZone.Grid -> when (dir) {
                Dir.Left -> if (s.grid > 0) set { copy(grid = grid - 1) } else openSide()
                Dir.Right -> if (s.grid < n - 1) set { copy(grid = grid + 1) }
                Dir.Down -> set { copy(zone = HomeZone.Panel, panelButton = 0) }
                Dir.Up -> if (data.topItem != null) set { copy(zone = HomeZone.Top) }
            }
            HomeZone.Panel -> when (dir) {
                Dir.Left -> if (s.panelButton > 0) set { copy(panelButton = panelButton - 1) } else set { copy(zone = HomeZone.Grid) }
                Dir.Right -> if (s.panelButton < PanelButton.entries.size - 1) set { copy(panelButton = panelButton + 1) }
                Dir.Up -> set { copy(zone = HomeZone.Grid) }
                Dir.Down -> Unit
            }
            HomeZone.Top -> when (dir) {
                Dir.Down -> set { copy(zone = HomeZone.Grid) }
                Dir.Left -> openSide()
                else -> Unit
            }
            null -> when (dir) {
                Dir.Left -> openSide()
                Dir.Up -> if (data.topItem != null) set { copy(zone = HomeZone.Top) }
                else -> Unit
            }
        }
    }

    private fun homeOk() {
        val s = state
        if (s.moving) {
            s.moveOrder?.let(actions::saveOrder)
            set { copy(moveOrder = null, moveSnapshot = null) }
            return actions.toast("Nuevo orden guardado")
        }
        s.radial?.let { r ->
            when (r.option) {
                RadialOption.Open -> { set { copy(radial = null) }; actions.openApp(r.packageName) }
                RadialOption.Move -> set { copy(radial = null, moveSnapshot = data.apps, moveOrder = data.apps) }
                RadialOption.Info -> set { copy(radial = null, info = InfoBox(r.packageName)) }
                RadialOption.Hide -> Unit
            }
            return
        }
        val app = data.apps.getOrNull(s.grid)
        when (homeZone()) {
            HomeZone.Grid -> app?.let(actions::openApp)
            HomeZone.Panel -> app?.let {
                when (PanelButton.entries[s.panelButton]) {
                    PanelButton.Open -> actions.openApp(it)
                    PanelButton.Info -> set { copy(info = InfoBox(it)) }
                }
            }
            HomeZone.Top -> when (data.topItem) {
                TopItem.Wifi -> actions.quick(QuickSetting.Wifi)
                TopItem.WeatherRetry -> { actions.retryWeather(); set { copy(zone = HomeZone.Grid) } }
                null -> Unit
            }
            null -> if (data.ready) {
                go(CityScreen.Settings)
                set { copy(settingsRow = data.settingsRows.indexOf(SettingsRow.Hidden), settingsCol = 0) }
            }
        }
    }

    private fun settingsDirection(dir: Dir) {
        val s = state
        val rows = data.settingsRows
        val r = s.settingsRow.coerceIn(0, rows.size - 1)
        val (row, c) = settingsFocus()
        val firstRight = rows.indexOfFirst { it == SettingsRow.Forecast || it == SettingsRow.Hidden }
        fun at(index: Int, col: Int) = set { copy(settingsRow = index, settingsCol = col) }
        val right = row == SettingsRow.Forecast || row == SettingsRow.Hidden
        if (row == SettingsRow.Hidden) {
            when (dir) {
                Dir.Up -> if (c > 0) at(r, c - 1) else {
                    val prev = rows[r - 1]
                    at(r - 1, if (prev == SettingsRow.Forecast) 0 else data.columns(prev) - 1)
                }
                Dir.Down -> if (c < data.columns(row) - 1) at(r, c + 1)
                Dir.Left -> at(rows.indexOf(SettingsRow.Night), NightMode.entries.size - 1)
                Dir.Right -> Unit
            }
            return
        }
        if (right) {
            when (dir) {
                Dir.Down -> at(r + 1, 0)
                Dir.Left -> at(rows.indexOf(SettingsRow.Quick), QuickSetting.entries.size - 1)
                else -> Unit
            }
            return
        }
        when (dir) {
            Dir.Left -> if (c > 0) at(r, c - 1) else openSide()
            Dir.Right -> if (c < data.columns(row) - 1) at(r, c + 1) else at(firstRight, 0)
            Dir.Up -> if (r > 0) at(r - 1, minOf(c, data.columns(rows[r - 1]) - 1))
            Dir.Down -> {
                val next = rows[r + 1]
                val nextRight = next == SettingsRow.Forecast || next == SettingsRow.Hidden
                at(r + 1, if (nextRight) 0 else minOf(c, data.columns(next) - 1))
            }
        }
    }

    private fun settingsOk() {
        val (row, c) = settingsFocus()
        when (row) {
            SettingsRow.Quick -> actions.quick(QuickSetting.entries[c])
            SettingsRow.Timer -> actions.setTimer(TimerOptions[c])
            SettingsRow.Night -> actions.setNightMode(NightMode.entries[c])
            SettingsRow.Forecast -> actions.retryWeather()
            SettingsRow.Hidden -> {
                val pkg = data.hidden.getOrNull(c) ?: return go(CityScreen.Home)
                actions.showApp(pkg)
                set { copy(settingsCol = (c).coerceAtMost((data.hidden.size - 2).coerceAtLeast(0))) }
            }
        }
    }

    private fun hide(packageName: String) {
        val n = data.apps.size
        actions.hideApp(packageName)
        set { copy(grid = grid.coerceAtMost((n - 2).coerceAtLeast(0))) }
    }

    private fun cancelMove() {
        val s = state
        val snap = s.moveSnapshot ?: data.apps
        val pkg = s.moveOrder?.getOrNull(s.grid)
        set { copy(moveOrder = null, moveSnapshot = null, grid = snap.indexOf(pkg).coerceAtLeast(0)) }
        actions.toast("Movimiento cancelado")
    }

    private fun openSide() = set { copy(side = true, sideIndex = screen.ordinal) }

    private fun go(screen: CityScreen) =
        set { copy(screen = screen, side = false, zone = HomeZone.Grid, radial = null, info = null) }

    private inline fun set(change: CityState.() -> CityState) {
        state = state.change()
    }

    private companion object {
        const val RING_MS = 1100L
    }
}
