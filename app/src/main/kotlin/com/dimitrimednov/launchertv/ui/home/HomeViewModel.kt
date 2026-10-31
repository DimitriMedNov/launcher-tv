package com.dimitrimednov.launchertv.ui.home

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dimitrimednov.launchertv.data.AppList
import com.dimitrimednov.launchertv.data.AppsRepository
import com.dimitrimednov.launchertv.data.BackgroundsRepository
import com.dimitrimednov.launchertv.data.DemoData
import com.dimitrimednov.launchertv.data.DeviceStatus
import com.dimitrimednov.launchertv.data.InputKind
import com.dimitrimednov.launchertv.data.InputsRepository
import com.dimitrimednov.launchertv.data.LauncherApp
import com.dimitrimednov.launchertv.data.NowPlaying
import com.dimitrimednov.launchertv.data.NowPlayingRepository
import com.dimitrimednov.launchertv.data.SPOTIFY_PACKAGE
import com.dimitrimednov.launchertv.data.TvInput
import com.dimitrimednov.launchertv.data.Weather
import com.dimitrimednov.launchertv.data.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Clima: cargando, listo o con error (si falla, la interfaz oculta solo ese bloque). */
sealed interface WeatherState {
    data object Loading : WeatherState
    data class Ready(val weather: Weather) : WeatherState
    data object Failed : WeatherState
}

/** App que se está abriendo: la tarjeta crece hasta llenar la pantalla y luego se lanza la app. */
data class Opening(val app: LauncherApp, val fromGrid: Boolean, val gridIndex: Int)

class HomeViewModel(application: Application) : AndroidViewModel(application), NavActions {
    private val context = application
    private val appsRepo = AppsRepository(application)
    private val weatherRepo = WeatherRepository()
    private val inputsRepo = InputsRepository(application)
    private val nowPlayingRepo = NowPlayingRepository(application)
    private val device = DeviceStatus(application)
    private val backgroundsRepo = BackgroundsRepository(application)

    private val _apps = MutableStateFlow<AppList?>(null)
    /** null mientras se leen las apps la primera vez (estado "Cargando"). */
    val apps: StateFlow<AppList?> = _apps.asStateFlow()

    private val _weather = MutableStateFlow<WeatherState>(WeatherState.Loading)
    val weather: StateFlow<WeatherState> = _weather.asStateFlow()

    /** Modo demo: los datos de ejemplo del diseño, para compararlo y sacar capturas (ver [DemoData]). */
    private val demo = MutableStateFlow(false)

    val inputs: StateFlow<List<TvInput>> =
        combine(inputsRepo.inputs(), demo) { real, isDemo -> if (isDemo) DemoData.inputs else real }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val nowPlaying: StateFlow<Map<String, NowPlaying>> =
        combine(nowPlayingRepo.sessions(), demo) { real, isDemo -> if (isDemo) DemoData.nowPlaying else real }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())
    val online: StateFlow<Boolean> = device.online().stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val wifiEnabled: StateFlow<Boolean> = device.wifiEnabled().stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast.asStateFlow()
    private var toastJob: Job? = null

    private val _opening = MutableStateFlow<Opening?>(null)
    val opening: StateFlow<Opening?> = _opening.asStateFlow()

    val navigator = Navigator(viewModelScope, this)

    /** Fondo del inicio y del protector: tus fotos y videos, uno tras otro. */
    val background = BackgroundRotation(viewModelScope)

    init {
        viewModelScope.launch {
            combine(_apps, inputs, online) { apps, inputs, online -> Triple(apps, inputs, online) }
                .collect { (apps, inputs, online) ->
                    navigator.offline = !online
                    navigator.data = NavData(
                        apps = apps?.visible?.map { it.packageName }.orEmpty(),
                        hidden = apps?.hidden?.map { it.packageName }.orEmpty(),
                        inputs = inputs.map { it.id },
                        heroButtons = if (online) 2 else 1,
                        ready = apps != null,
                    )
                    navigator.clamp()
                }
        }
        viewModelScope.launch {
            while (true) {
                val fetched = runCatching { weatherRepo.fetch() }.fold({ WeatherState.Ready(it) }, { WeatherState.Failed })
                // En modo demo se queda el clima de ejemplo del diseño.
                _weather.value = if (demo.value) WeatherState.Ready(DemoData.weather) else fetched
                delay(if (_weather.value is WeatherState.Ready) WEATHER_REFRESH_MS else WEATHER_RETRY_MS)
            }
        }
    }

    /** Se llama al volver al launcher: relee las apps (por si se instaló o quitó alguna). */
    fun onResume() {
        _opening.value = null
        reloadApps()
        viewModelScope.launch { background.setItems(backgroundsRepo.load()) }
    }

    fun enableDemo() {
        demo.value = true
        _weather.value = WeatherState.Ready(DemoData.weather)
        reloadApps()
    }

    private fun reloadApps() {
        viewModelScope.launch { _apps.value = if (demo.value) DemoData.apps else appsRepo.load() }
    }

    private fun app(packageName: String) =
        _apps.value?.let { it.visible + it.hidden }?.firstOrNull { it.packageName == packageName }

    override fun openApp(packageName: String, fromGrid: Boolean) {
        val app = app(packageName) ?: return
        val index = _apps.value?.visible?.indexOf(app) ?: 0
        _opening.value = Opening(app, fromGrid, index)
        viewModelScope.launch {
            delay(OPEN_ANIMATION_MS)
            launch(Intent(Intent.ACTION_MAIN).setComponent(app.component))
            delay(OPEN_TIMEOUT_MS)
            _opening.value = null
        }
    }

    override fun heroPrimary(packageName: String) {
        if (packageName == SPOTIFY_PACKAGE && nowPlaying.value.containsKey(packageName)) {
            nowPlayingRepo.togglePlayback(packageName)
        } else {
            openApp(packageName, fromGrid = false)
        }
    }

    override fun appInfo(packageName: String) = launch(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)),
    )

    override fun hideApp(packageName: String) {
        appsRepo.setHidden(packageName, true)
        reloadApps()
    }

    override fun showApp(packageName: String) {
        appsRepo.setHidden(packageName, false)
        reloadApps()
        toast("${app(packageName)?.name ?: "La app"} vuelve al inicio")
    }

    override fun saveOrder(packages: List<String>) {
        appsRepo.saveOrder(packages)
        reloadApps()
    }

    override fun openInput(inputId: String) {
        val input = inputs.value.firstOrNull { it.id == inputId } ?: return
        if (!input.hasSignal && input.port != null) return toast("${input.name}: sin señal")
        toast("Cambiando a ${input.name}…")
        inputsRepo.switchIntent(input)?.let(::launch) ?: toast("No se pudo cambiar de entrada")
    }

    override fun setInputKind(inputId: String, kind: InputKind) {
        inputsRepo.setKind(inputId, kind)
        toast("Ícono guardado: ${kind.label}")
    }

    override fun inputInfo(inputId: String) {
        val input = inputs.value.firstOrNull { it.id == inputId } ?: return
        val where = input.port?.let { "HDMI $it" } ?: "Antena"
        toast("$where · ${if (input.hasSignal) "Con señal" else "Sin señal"}")
    }

    override fun setting(item: SettingItem) = when (item) {
        SettingItem.Wifi -> openWifiSettings()
        SettingItem.Picture -> launch(Intent(Settings.ACTION_DISPLAY_SETTINGS))
        SettingItem.Sound -> launch(Intent(Settings.ACTION_SOUND_SETTINGS))
        SettingItem.Timer -> launch(Intent("android.settings.TIMER_SETTINGS"))
        SettingItem.Android -> launch(Intent(Settings.ACTION_SETTINGS))
        SettingItem.HiddenApps -> Unit
    }

    override fun openWifiSettings() = launch(Intent(Settings.ACTION_WIFI_SETTINGS))

    override fun toast(message: String) {
        toastJob?.cancel()
        _toast.value = message
        toastJob = viewModelScope.launch {
            delay(TOAST_MS)
            _toast.value = null
        }
    }

    private fun launch(intent: Intent) {
        try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: ActivityNotFoundException) {
            _opening.value = null
            toast("Esta tele no tiene esa pantalla")
        } catch (_: SecurityException) {
            _opening.value = null
            toast("La tele no permite abrir eso")
        }
    }

    private companion object {
        const val WEATHER_REFRESH_MS = 30 * 60_000L
        const val WEATHER_RETRY_MS = 5 * 60_000L
        const val OPEN_ANIMATION_MS = 650L
        const val OPEN_TIMEOUT_MS = 4_000L
        const val TOAST_MS = 1_900L
    }
}
