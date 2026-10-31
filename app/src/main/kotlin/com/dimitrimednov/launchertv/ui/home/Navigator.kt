package com.dimitrimednov.launchertv.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dimitrimednov.launchertv.data.InputKind
import com.dimitrimednov.launchertv.ui.theme.Layout
import com.dimitrimednov.launchertv.ui.theme.Motion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Lo que pasa fuera de la navegación cuando el usuario elige algo. */
interface NavActions {
    fun openApp(packageName: String, fromGrid: Boolean)
    fun heroPrimary(packageName: String)
    fun appInfo(packageName: String)
    fun hideApp(packageName: String)
    fun showApp(packageName: String)
    fun saveOrder(packages: List<String>)
    fun openInput(inputId: String)
    fun setInputKind(inputId: String, kind: InputKind)
    fun inputInfo(inputId: String)
    fun setting(item: SettingItem)
    fun openWifiSettings()
    fun toast(message: String)
}

/**
 * Máquina de estados del control remoto, copiada del `keyDown` del diseño y ampliada con
 * el menú de app, el modo Mover, las apps ocultas y el selector de ícono.
 */
class Navigator(private val scope: CoroutineScope, private val actions: NavActions) {
    var state by mutableStateOf(NavState())
        private set
    var data = NavData()
    var offline = false
    private var backAt = 0L
    private val cols = Layout.GRID_COLUMNS

    fun onDirection(dir: Dir) {
        val s = state
        if (s.saver) return closeSaver()
        if (!data.ready) return
        if (s.panel != null) return panelDirection(dir)
        if (s.moving != null) return moveDirection(dir)
        when (s.zone) {
            Zone.Rail -> when (dir) {
                Dir.Up -> goSection(s.section.ordinal - 1)
                Dir.Down -> goSection(s.section.ordinal + 1)
                Dir.Right -> enter(s.section.zone)
                Dir.Left -> Unit
            }
            Zone.Hero -> when (dir) {
                Dir.Left -> if (s.heroButton > 0) set { copy(heroButton = heroButton - 1) } else enter(Zone.Rail)
                Dir.Right -> if (s.heroButton < data.heroButtons - 1) set { copy(heroButton = heroButton + 1) }
                Dir.Down -> tilt(dir) { copy(zone = Zone.Grid) }
                Dir.Up -> Unit
            }
            Zone.Grid -> gridDirection(dir)
            Zone.Inputs -> when (dir) {
                Dir.Left -> if (s.input > 0) tilt(dir) { copy(input = input - 1) } else enter(Zone.Rail)
                Dir.Right -> if (s.input < data.inputs.size - 1) tilt(dir) { copy(input = input + 1) }
                else -> Unit
            }
            Zone.Settings -> when (dir) {
                Dir.Left -> enter(Zone.Rail)
                Dir.Up -> if (s.setting > 0) set { copy(setting = setting - 1) }
                Dir.Down -> if (s.setting < SettingItem.entries.size - 1) set { copy(setting = setting + 1) }
                Dir.Right -> Unit
            }
        }
    }

    fun onOk() {
        val s = state
        if (s.saver) return closeSaver()
        if (!data.ready) return
        s.panel?.let { return panelOk(it) }
        if (s.moving != null) return finishMove(save = true)
        when (s.zone) {
            Zone.Rail -> enter(s.section.zone)
            Zone.Hero -> press("hero${s.heroButton}") {
                val app = data.apps.getOrNull(s.grid)
                when {
                    offline -> actions.openWifiSettings()
                    app == null -> Unit
                    s.heroButton == 0 -> actions.heroPrimary(app)
                    else -> actions.appInfo(app)
                }
            }
            Zone.Grid -> {
                val app = data.apps.getOrNull(s.grid)
                if (app == null) set { copy(panel = Panel.HiddenApps, panelIndex = 0) }
                else press("tile$app") { actions.openApp(app, fromGrid = true) }
            }
            Zone.Inputs -> data.inputs.getOrNull(s.input)?.let { id -> press("input$id") { actions.openInput(id) } }
            Zone.Settings -> {
                val item = SettingItem.entries[s.setting]
                if (item == SettingItem.HiddenApps) set { copy(panel = Panel.HiddenApps, panelIndex = 0) }
                else actions.setting(item)
            }
        }
    }

    /** OK mantenido 600 ms: menú de la app o de la entrada enfocada. */
    fun onLongOk() {
        val s = state
        if (s.saver || s.panel != null || s.moving != null || !data.ready) return
        when (s.zone) {
            Zone.Grid -> data.apps.getOrNull(s.grid)?.let { set { copy(panel = Panel.AppMenu(it), panelIndex = 0) } }
            Zone.Inputs -> data.inputs.getOrNull(s.input)?.let { set { copy(panel = Panel.InputMenu(it), panelIndex = 0) } }
            else -> Unit
        }
    }

    fun onBack() {
        val s = state
        if (s.saver) return closeSaver()
        when {
            s.panel is Panel.IconPicker -> set { copy(panel = Panel.InputMenu(s.panel.inputId), panelIndex = 1) }
            s.panel != null -> set { copy(panel = null) }
            s.moving != null -> finishMove(save = false)
            s.zone == Zone.Grid && s.grid >= cols -> tilt(Dir.Up) { copy(grid = grid % cols) }
            s.zone != Zone.Rail -> enter(Zone.Rail)
            else -> {
                val now = System.currentTimeMillis()
                if (now - backAt < Motion.DOUBLE_BACK_MS) {
                    backAt = 0
                    openSaver()
                } else {
                    backAt = now
                    actions.toast("Presiona Atrás otra vez para el protector")
                }
            }
        }
    }

    /** Botón Home estando ya en el launcher: vuelve al principio. */
    fun reset() {
        state = NavState()
    }

    fun openSaver() = set { copy(saver = true, panel = null, moving = null, moveOrder = null) }

    fun closeSaver() = set { copy(saver = false) }

    /** Ajusta los índices cuando cambian las listas (app desinstalada, entrada nueva…). */
    fun clamp() = set {
        copy(
            grid = grid.coerceIn(0, (data.apps.size - 1).coerceAtLeast(0)),
            input = input.coerceIn(0, (data.inputs.size - 1).coerceAtLeast(0)),
            heroButton = heroButton.coerceIn(0, data.heroButtons - 1),
        )
    }

    private fun gridDirection(dir: Dir) {
        val s = state
        val n = data.apps.size
        if (n == 0) {
            if (dir == Dir.Left) enter(Zone.Rail)
            if (dir == Dir.Up) enter(Zone.Hero)
            return
        }
        val col = s.grid % cols
        val next = when (dir) {
            Dir.Left -> if (col == 0) return enter(Zone.Rail) else s.grid - 1
            Dir.Right -> if (col < cols - 1 && s.grid + 1 < n) s.grid + 1 else s.grid
            Dir.Up -> if (s.grid < cols) return set { copy(zone = Zone.Hero, heroButton = 0) } else s.grid - cols
            Dir.Down -> minOf(n - 1, s.grid + cols)
        }
        if (next != s.grid) tilt(dir) { copy(grid = next) }
    }

    private fun moveDirection(dir: Dir) {
        val order = state.moveOrder ?: return
        val from = order.indexOf(state.moving)
        val to = when (dir) {
            Dir.Left -> from - 1
            Dir.Right -> from + 1
            Dir.Up -> from - cols
            Dir.Down -> from + cols
        }
        if (to !in order.indices) return
        val next = order.toMutableList().apply { add(to, removeAt(from)) }
        tilt(dir) { copy(moveOrder = next, grid = to) }
    }

    private fun finishMove(save: Boolean) {
        val s = state
        val order = s.moveOrder
        if (save && order != null) {
            actions.saveOrder(order)
            actions.toast("Orden guardado")
        }
        val index = (if (save) order else data.apps)?.indexOf(s.moving)?.takeIf { it >= 0 } ?: s.grid
        set { copy(moving = null, moveOrder = null, grid = index) }
    }

    private fun panelDirection(dir: Dir) {
        val s = state
        val size = panelSize(s.panel ?: return)
        val step = if (s.panel is Panel.IconPicker) {
            when (dir) { Dir.Left -> -1; Dir.Right -> 1; Dir.Up -> -2; Dir.Down -> 2 }
        } else {
            when (dir) { Dir.Up -> -1; Dir.Down -> 1; else -> 0 }
        }
        val next = s.panelIndex + step
        if (step != 0 && next in 0 until size) set { copy(panelIndex = next) }
    }

    private fun panelOk(panel: Panel) {
        val i = state.panelIndex
        when (panel) {
            is Panel.AppMenu -> when (AppMenuItem.entries[i]) {
                AppMenuItem.Open -> close { actions.openApp(panel.packageName, fromGrid = true) }
                AppMenuItem.Move -> set {
                    copy(panel = null, moving = panel.packageName, moveOrder = data.apps, zone = Zone.Grid)
                }
                AppMenuItem.Hide -> close {
                    actions.hideApp(panel.packageName)
                    actions.toast("App oculta. Puedes mostrarla en Ajustes")
                }
                AppMenuItem.Info -> close { actions.appInfo(panel.packageName) }
            }
            is Panel.InputMenu -> when (InputMenuItem.entries[i]) {
                InputMenuItem.Open -> close { actions.openInput(panel.inputId) }
                InputMenuItem.Icon -> set { copy(panel = Panel.IconPicker(panel.inputId), panelIndex = 0) }
                InputMenuItem.Info -> close { actions.inputInfo(panel.inputId) }
            }
            is Panel.IconPicker -> close { actions.setInputKind(panel.inputId, InputKind.entries[i]) }
            Panel.HiddenApps -> data.hidden.getOrNull(i)?.let { pkg ->
                actions.showApp(pkg)
                val left = data.hidden.size - 1
                if (left <= 0) set { copy(panel = null) } else set { copy(panelIndex = panelIndex.coerceAtMost(left - 1)) }
            }
        }
    }

    private fun panelSize(panel: Panel) = when (panel) {
        is Panel.AppMenu -> AppMenuItem.entries.size
        is Panel.InputMenu -> InputMenuItem.entries.size
        is Panel.IconPicker -> InputKind.entries.size
        Panel.HiddenApps -> data.hidden.size
    }

    private fun goSection(index: Int) {
        val section = Section.entries.getOrNull(index) ?: return
        set { copy(section = section, lastDir = if (index > this.section.ordinal) Dir.Down else Dir.Up) }
    }

    private fun enter(zone: Zone) = set {
        val section = when (zone) {
            Zone.Hero, Zone.Grid -> Section.Inicio
            Zone.Inputs -> Section.Entradas
            Zone.Settings -> Section.Ajustes
            Zone.Rail -> section
        }
        copy(zone = zone, section = section)
    }

    private fun close(then: () -> Unit) {
        set { copy(panel = null) }
        then()
    }

    private fun press(id: String, then: () -> Unit) {
        set { copy(pressed = id) }
        scope.launch {
            delay(Motion.PRESS_MS.toLong())
            set { copy(pressed = null) }
            then()
        }
    }

    private fun tilt(dir: Dir, change: NavState.() -> NavState) =
        set { change().copy(lastDir = dir, tiltTick = tiltTick + 1) }

    private inline fun set(change: NavState.() -> NavState) {
        state = state.change()
    }
}
