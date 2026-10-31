package com.dimitrimednov.launchertv.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dimitrimednov.launchertv.data.AppList
import com.dimitrimednov.launchertv.data.LauncherApp
import com.dimitrimednov.launchertv.data.NowPlaying
import com.dimitrimednov.launchertv.ui.theme.DesignCanvas
import com.dimitrimednov.launchertv.ui.theme.Layout
import com.dimitrimednov.launchertv.ui.theme.Motion
import com.dimitrimednov.launchertv.ui.theme.Palette
import kotlinx.coroutines.delay

/** Pantalla de inicio completa: fondo, barra de arriba, sección actual, barra lateral y capas encima. */
@Composable
fun HomeScreen(vm: HomeViewModel) {
    val nav = vm.navigator
    val state = nav.state
    val apps by vm.apps.collectAsStateWithLifecycle()
    val weather by vm.weather.collectAsStateWithLifecycle()
    val inputs by vm.inputs.collectAsStateWithLifecycle()
    val nowPlaying by vm.nowPlaying.collectAsStateWithLifecycle()
    val online by vm.online.collectAsStateWithLifecycle()
    val background by vm.background.current.collectAsStateWithLifecycle()
    val loopVideo by vm.background.loopVideo.collectAsStateWithLifecycle()
    val wifi by vm.wifiEnabled.collectAsStateWithLifecycle()
    val toast by vm.toast.collectAsStateWithLifecycle()
    val opening by vm.opening.collectAsStateWithLifecycle()
    val now = rememberNow()
    val boot = rememberBootStage()

    val focus = remember { FocusRequester() }
    var lastKeyAt by remember { mutableLongStateOf(0L) }
    LaunchedEffect(lastKeyAt, state.saver) {
        if (state.saver) return@LaunchedEffect
        delay(Motion.IDLE_SAVER_MS)
        if (vm.opening.value == null) nav.openSaver()
    }
    LaunchedEffect(state.saver) { vm.background.setSaver(state.saver) }
    LaunchedEffect(Unit) { focus.requestFocus() }

    val shownApps = displayedApps(apps, state)
    val focusedApp = shownApps.getOrNull(state.grid)
    val accent = if (state.section == Section.Inicio) focusedApp?.brand?.accent ?: Palette.Accent else Palette.Accent
    val ready = (weather as? WeatherState.Ready)?.weather?.takeIf { online }

    DesignCanvas {
        Box(
            Modifier
                .fillMaxSize()
                .remoteKeys(nav) { lastKeyAt = System.currentTimeMillis() }
                .focusRequester(focus)
                .focusable(),
        ) {
            HomeBackground(
                item = background,
                loop = vm.background.single || loopVideo,
                onVideoEnded = vm.background::videoEnded,
                dimmed = state.zone == Zone.Rail,
                faded = state.section != Section.Inicio,
                saver = state.saver,
                gridColumn = if (state.zone == Zone.Grid) state.grid % Layout.GRID_COLUMNS else null,
            )
            // Con el protector abierto se esconde todo menos el fondo.
            val uiAlpha by animateFloatAsState(if (state.saver) 0f else 1f, tween(800), label = "interfaz")
            Box(Modifier.fillMaxSize().graphicsLayer { alpha = uiAlpha }) {
                Box(Modifier.bootIn(boot >= 2, fromY = -24f)) {
                    TopBar(ready?.current, now)
                }
                SectionContent(state, Modifier.bootIn(boot >= 3, fromY = 40f)) { section ->
                    when (section) {
                        Section.Inicio -> Inicio(apps, shownApps, focusedApp, nowPlaying, online, state, accent)
                        Section.Entradas -> InputsSection(inputs, state)
                        Section.Ajustes -> SettingsSection(state, wifi, apps?.hidden?.size ?: 0, ready?.days)
                    }
                }
                Box(Modifier.bootIn(boot >= 2, fromX = -160f)) {
                    Rail(state.section, open = state.zone == Zone.Rail && state.panel == null, accent = accent)
                }
                SidePanel(state.panel, state.panelIndex, apps?.visible.orEmpty(), apps?.hidden.orEmpty(), inputs)
                Toast(toast, accent)
                OpenOverlay(opening, gridFocused = state.zone == Zone.Grid)
            }
            AnimatedVisibility(state.saver, enter = fadeIn(tween(800)), exit = fadeOut(tween(800))) {
                Screensaver(ready?.current, now, background.name)
            }
        }
    }
}

@Composable
private fun SectionContent(state: NavState, modifier: Modifier, content: @Composable (Section) -> Unit) {
    AnimatedContent(
        targetState = state.section,
        transitionSpec = {
            val down = targetState.ordinal > initialState.ordinal
            (fadeIn(tween(420)) + slideInVertically(Motion.ease(620)) { if (down) 60 else -60 }) togetherWith
                (fadeOut(tween(160)) + slideOutVertically(tween(170)) { if (down) -30 else 30 })
        },
        label = "sección",
        modifier = modifier.fillMaxSize(),
    ) { section -> Box(Modifier.fillMaxSize()) { content(section) } }
}

@Composable
private fun Inicio(
    apps: AppList?,
    shownApps: List<LauncherApp>,
    focusedApp: LauncherApp?,
    nowPlaying: Map<String, NowPlaying>,
    online: Boolean,
    state: NavState,
    accent: Color,
) {
    val heroAt = Modifier.offset(Layout.ContentLeft, Layout.SectionTop)
    if (apps == null) {
        HeroSkeleton(heroAt)
        GridSkeleton()
        return
    }
    val gridFocused = state.zone == Zone.Grid || state.moving != null
    val heroScale by animateFloatAsState(if (gridFocused) .88f else 1f, Motion.ease(560), label = "escala hero")
    val heroModifier = heroAt.graphicsLayer {
        transformOrigin = TransformOrigin(0f, 0f)
        scaleX = heroScale
        scaleY = heroScale
    }
    val heroFocus = state.heroButton.takeIf { state.zone == Zone.Hero && state.panel == null }
    when {
        !online -> OfflineHero(heroFocus != null, state.pressed == "hero0", heroModifier)
        focusedApp != null -> Hero(
            model = heroModel(focusedApp, nowPlaying[focusedApp.packageName]),
            dir = state.lastDir,
            focusedButton = heroFocus,
            pressed = state.pressed,
            accent = accent,
            modifier = heroModifier,
        )
    }
    if (shownApps.isEmpty()) {
        NoAppsMessage(focused = state.zone == Zone.Grid && state.panel == null, pressed = false)
    } else {
        AppGrid(shownApps, state, accent)
    }
}

/** Mientras se mueve una app se muestra el orden provisional. */
private fun displayedApps(apps: AppList?, state: NavState): List<LauncherApp> {
    val visible = apps?.visible.orEmpty()
    val order = state.moveOrder ?: return visible
    return order.mapNotNull { pkg -> visible.firstOrNull { it.packageName == pkg } }
}

/** Entrada de arranque: aparece desde un lado cuando le toca. */
@Composable
private fun Modifier.bootIn(shown: Boolean, fromX: Float = 0f, fromY: Float = 0f): Modifier {
    val p by animateFloatAsState(if (shown) 1f else 0f, Motion.ease(600), label = "arranque")
    return graphicsLayer {
        alpha = p
        translationX = (1 - p) * fromX
        translationY = (1 - p) * fromY
    }
}
