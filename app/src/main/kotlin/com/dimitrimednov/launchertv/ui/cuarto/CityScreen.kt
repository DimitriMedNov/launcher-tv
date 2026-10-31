package com.dimitrimednov.launchertv.ui.cuarto

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dimitrimednov.launchertv.ui.home.Dir
import com.dimitrimednov.launchertv.ui.home.WeatherState
import com.dimitrimednov.launchertv.ui.home.formatLongDate
import com.dimitrimednov.launchertv.ui.home.rememberNow
import com.dimitrimednov.launchertv.ui.theme.DesignCanvas
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

private const val IDLE_SAVER_MS = 120_000L
private const val HOLD_MS = 500L

/** Launcher del cuarto: la ciudad de noche (`diseno/Launcher TV Batman/Launcher Cuarto v6.dc.html`). */
@Composable
fun CityApp(vm: CityViewModel) {
    val nav = vm.navigator
    val s = nav.state
    val apps by vm.apps.collectAsStateWithLifecycle()
    val weather by vm.weather.collectAsStateWithLifecycle()
    val inputs by vm.inputs.collectAsStateWithLifecycle()
    val nowPlaying by vm.nowPlaying.collectAsStateWithLifecycle()
    val online by vm.online.collectAsStateWithLifecycle()
    val nightMode by vm.nightMode.collectAsStateWithLifecycle()
    val timer by vm.timer.collectAsStateWithLifecycle()
    val toast by vm.toast.collectAsStateWithLifecycle()
    val now = rememberNow()
    val nowMs = rememberSecondsWhile(timer != null)
    val night = nightMode.isNight(now.hour)

    val focus = remember { FocusRequester() }
    var lastKeyAt by remember { mutableLongStateOf(0L) }
    LaunchedEffect(lastKeyAt, s.saver) {
        if (s.saver) return@LaunchedEffect
        delay(IDLE_SAVER_MS)
        nav.openSaver()
    }
    LaunchedEffect(Unit) { focus.requestFocus() }

    val shown = apps?.visible?.let { visible ->
        s.moveOrder?.mapNotNull { pkg -> visible.firstOrNull { it.packageName == pkg } } ?: visible
    }
    val zone = nav.homeZone()
    val clock = clockOf(now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())

    DesignCanvas(fontFamily = Barlow) {
        CityTheme(night) {
            val c = city
            Box(
                Modifier
                    .fillMaxSize()
                    .background(c.bg)
                    .cityKeys(nav) { lastKeyAt = System.currentTimeMillis() }
                    .focusRequester(focus)
                    .focusable(),
            ) {
                HeaderRule()
                RailDots(s.screen)
                HeaderLeft(
                    title = when (s.screen) {
                        CityScreen.Home -> greeting(now.hour)
                        CityScreen.Inputs -> "Entradas"
                        CityScreen.Settings -> "Ajustes"
                    },
                    offline = s.screen == CityScreen.Home && !online,
                    wifiFocused = s.base && zone == HomeZone.Top && nav.data.topItem == TopItem.Wifi,
                    moving = s.screen == CityScreen.Home && s.moving,
                )
                HeaderRight(
                    timerLeft = timer?.let { mmssOf((it.endAt - nowMs).coerceAtLeast(0)) },
                    night = night,
                    nightShort = nightMode.short,
                    weather = weather,
                    online = online,
                    weatherFocused = s.base && s.screen == CityScreen.Home && zone == HomeZone.Top && nav.data.topItem == TopItem.WeatherRetry,
                    clock = clock,
                    modifier = Modifier.align(Alignment.TopEnd),
                )
                AnimatedContent(
                    s.screen,
                    transitionSpec = { (fadeIn(cityTween(280)) + slideInVertically(cityTween(280)) { 18 }) togetherWith fadeOut(tween(160)) },
                    label = "pantalla",
                ) { screen ->
                    Box(Modifier.fillMaxSize()) {
                        when (screen) {
                            CityScreen.Home -> HomeContent(HomeModel(shown, nowPlaying, s, zone))
                            CityScreen.Inputs -> InputsContent(inputs, s)
                            CityScreen.Settings -> SettingsContent(
                                SettingsModel(
                                    state = s, focus = nav.settingsFocus(), timer = timer?.let { it.minutes to it.endAt },
                                    now = nowMs, nightMode = nightMode, night = night, weather = weather, online = online,
                                    hidden = apps?.hidden.orEmpty(), wifiSub = if (online) "Conectado" else "Sin conexión",
                                ),
                            )
                        }
                    }
                }
                SideMenu(s.side, s.sideIndex, s.screen)
                val infoApp = s.info?.let { info -> apps?.let { it.visible + it.hidden }?.firstOrNull { it.packageName == info.packageName } }
                InfoWindow(
                    info = s.info,
                    name = infoApp?.name.orEmpty(),
                    color = infoApp?.let(::appColor) ?: c.sf2,
                    rows = infoApp?.let { app ->
                        val vi = shown?.indexOf(app) ?: -1
                        listOf(
                            "Lo último" to (nowPlaying[app.packageName]?.title ?: "Sin actividad reciente"),
                            "Lugar en el inicio" to if (vi >= 0) "${vi + 1} de ${shown?.size}" else "Oculta",
                            "Opciones" to "Mantén OK sobre la app para moverla u ocultarla",
                        )
                    }.orEmpty(),
                )
                CityToast(toast, Modifier.align(Alignment.BottomCenter))
                AnimatedVisibility(s.saver, enter = fadeIn(tween(800)), exit = fadeOut(tween(600))) {
                    CitySaver(clock, formatLongDate(now).replace(",", ""), weatherLong((weather as? WeatherState.Ready)?.weather, online))
                }
                Storm(dim = s.saver)
            }
        }
    }
}

private fun greeting(hour: Int) = when {
    hour < 7 -> "La ciudad duerme"
    hour < 12 -> "Buen día, ciudad"
    hour < 19 -> "La tarde avanza"
    else -> "La noche es tuya"
}

/** "4:12" y "p. m." por separado, como en el diseño. */
fun clockOf(epochMs: Long): Pair<String, String> {
    val t = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMs), ZoneId.systemDefault())
    val h = t.hour % 12
    return "${if (h == 0) 12 else h}:${"%02d".format(t.minute)}" to if (t.hour < 12) "a. m." else "p. m."
}

fun mmssOf(ms: Long): String {
    val t = (ms + 999) / 1000
    return "${t / 60}:${"%02d".format(t % 60)}"
}

/** Hora en milisegundos que avanza cada segundo, solo mientras [active] (la cuenta del temporizador). */
@Composable
private fun rememberSecondsWhile(active: Boolean): Long {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(active) {
        now = System.currentTimeMillis()
        while (active) {
            delay(1_000L - System.currentTimeMillis() % 1_000L)
            now = System.currentTimeMillis()
        }
    }
    return now
}

/**
 * Teclas del control. OK corto = activar; OK mantenido 500 ms = menú radial; en "Ocultar",
 * mantener OK llena el anillo. Atrás nunca cierra el launcher.
 */
@Composable
private fun Modifier.cityKeys(nav: CityNavigator, onAnyKey: () -> Unit): Modifier {
    val ok = remember { OkPress() }
    return onPreviewKeyEvent { event ->
        val down = event.type == KeyEventType.KeyDown
        val up = event.type == KeyEventType.KeyUp
        val native = event.nativeKeyEvent
        if (down) onAnyKey()
        if (nav.state.saver && down && native.repeatCount == 0) {
            nav.closeSaver()
            ok.swallow = true
            return@onPreviewKeyEvent true
        }
        // La tecla que cerró el protector no hace nada más al soltarse.
        if (ok.swallow) {
            if (up) ok.swallow = false
            return@onPreviewKeyEvent true
        }
        when (event.key) {
            Key.DirectionUp -> { if (down) nav.onDirection(Dir.Up); true }
            Key.DirectionDown -> { if (down) nav.onDirection(Dir.Down); true }
            Key.DirectionLeft -> { if (down) nav.onDirection(Dir.Left); true }
            Key.DirectionRight -> { if (down) nav.onDirection(Dir.Right); true }
            Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
                when {
                    down && native.repeatCount == 0 -> {
                        if (nav.wantsRing()) { ok.ring = true; nav.startRing() } else ok.start(native.eventTime)
                    }
                    down && !ok.ring && ok.downAt >= 0 && !ok.held && (native.isLongPress || native.eventTime - ok.downAt >= HOLD_MS) -> {
                        ok.held = true
                        nav.onHold()
                    }
                    up -> {
                        if (ok.ring) nav.cancelRing() else if (!ok.held && ok.downAt >= 0) nav.onOk()
                        ok.reset()
                    }
                }
                true
            }
            Key.Back, Key.Escape -> { if (up) nav.onBack(); true }
            else -> false
        }
    }
}

private class OkPress {
    var downAt = -1L
    var held = false
    var ring = false
    var swallow = false

    fun start(time: Long) {
        downAt = time
        held = false
    }

    fun reset() {
        downAt = -1L
        held = false
        ring = false
    }
}
