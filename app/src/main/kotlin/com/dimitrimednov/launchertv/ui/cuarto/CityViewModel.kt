package com.dimitrimednov.launchertv.ui.cuarto

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dimitrimednov.launchertv.data.AppList
import com.dimitrimednov.launchertv.data.AppsRepository
import com.dimitrimednov.launchertv.data.DemoData
import com.dimitrimednov.launchertv.data.DeviceStatus
import com.dimitrimednov.launchertv.data.InputsRepository
import com.dimitrimednov.launchertv.data.NowPlaying
import com.dimitrimednov.launchertv.data.NowPlayingRepository
import com.dimitrimednov.launchertv.data.SleepTimer
import com.dimitrimednov.launchertv.data.TvInput
import com.dimitrimednov.launchertv.data.WeatherRepository
import com.dimitrimednov.launchertv.ui.home.WeatherState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Datos y acciones del launcher del cuarto. Reutiliza los repositorios del launcher de la sala. */
class CityViewModel(application: Application) : AndroidViewModel(application), CityActions {
    private val context = application
    private val appsRepo = AppsRepository(application)
    private val weatherRepo = WeatherRepository()
    private val inputsRepo = InputsRepository(application)
    private val nowPlayingRepo = NowPlayingRepository(application)
    private val device = DeviceStatus(application)
    private val prefs = application.getSharedPreferences("cuarto", Context.MODE_PRIVATE)

    private val demo = MutableStateFlow(false)

    private val _apps = MutableStateFlow<AppList?>(null)
    val apps: StateFlow<AppList?> = _apps.asStateFlow()

    private val _weather = MutableStateFlow<WeatherState>(WeatherState.Loading)
    val weather: StateFlow<WeatherState> = _weather.asStateFlow()

    /** Solo HDMI: el cuarto no usa antena. null mientras se leen la primera vez. */
    val inputs: StateFlow<List<TvInput>?> =
        combine(inputsRepo.inputs(), demo) { real, isDemo -> (if (isDemo) DemoData.inputs else real).filter { it.port != null } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val nowPlaying: StateFlow<Map<String, NowPlaying>> =
        combine(nowPlayingRepo.sessions(), demo) { real, isDemo -> if (isDemo) DemoData.nowPlaying else real }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())
    val online: StateFlow<Boolean> = device.online().stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val _nightMode = MutableStateFlow(readNightMode(application))
    val nightMode: StateFlow<NightMode> = _nightMode.asStateFlow()

    val timer = SleepTimer.running

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast.asStateFlow()
    private var toastJob: Job? = null
    private var weatherJob: Job? = null

    val navigator = CityNavigator(viewModelScope, this)

    init {
        SleepTimer.load(application)
        viewModelScope.launch {
            combine(_apps, inputs.map { it?.size ?: 0 }, online, _weather) { apps, inputs, online, weather ->
                CityData(
                    ready = apps != null,
                    apps = apps?.visible?.map { it.packageName }.orEmpty(),
                    hidden = apps?.hidden?.map { it.packageName }.orEmpty(),
                    inputs = inputs,
                    offline = !online,
                    weatherError = online && weather is WeatherState.Failed,
                    forecastError = !online || weather is WeatherState.Failed,
                )
            }.collect {
                navigator.data = it
                navigator.clamp()
            }
        }
        startWeather()
    }

    private fun startWeather() {
        weatherJob?.cancel()
        weatherJob = viewModelScope.launch {
            while (true) {
                val fetched = runCatching { weatherRepo.fetch() }
                    .fold({ WeatherState.Ready(it) }, { WeatherState.Failed })
                _weather.value = if (demo.value) WeatherState.Ready(DemoData.weather) else fetched
                delay(if (_weather.value is WeatherState.Ready) 30 * 60_000L else 5 * 60_000L)
            }
        }
    }

    fun onResume() {
        reloadApps()
        SleepTimer.load(context)
    }

    fun enableDemo() {
        demo.value = true
        _weather.value = WeatherState.Ready(DemoData.weather)
        reloadApps()
    }

    private fun reloadApps() {
        viewModelScope.launch { _apps.value = if (demo.value) DemoData.apps else appsRepo.load() }
    }

    private fun name(packageName: String) =
        _apps.value?.let { it.visible + it.hidden }?.firstOrNull { it.packageName == packageName }?.name ?: "La app"

    override fun openApp(packageName: String) {
        val app = _apps.value?.visible?.firstOrNull { it.packageName == packageName } ?: return
        toast("Abriendo ${app.name}. Que empiece la función.")
        launch(Intent(Intent.ACTION_MAIN).setComponent(app.component))
    }

    override fun appSettings(packageName: String) = launch(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)),
    )

    override fun hideApp(packageName: String) {
        appsRepo.setHidden(packageName, true)
        reloadApps()
        toast("${name(packageName)} se fue a las sombras · Ajustes › Apps ocultas")
    }

    override fun showApp(packageName: String) {
        appsRepo.setHidden(packageName, false)
        reloadApps()
        toast("${name(packageName)} vuelve a la ciudad")
    }

    override fun saveOrder(packages: List<String>) {
        appsRepo.saveOrder(packages)
        reloadApps()
    }

    override fun openInput(index: Int) {
        val input = inputs.value?.getOrNull(index) ?: return
        if (!input.hasSignal) return toast("${input.name} no tiene señal")
        toast("Cambiando a ${input.name}…")
        inputsRepo.switchIntent(input)?.let(::launch) ?: toast("No se pudo cambiar de entrada")
    }

    override fun quick(item: QuickSetting) = launch(
        Intent(
            when (item) {
                QuickSetting.Wifi -> Settings.ACTION_WIFI_SETTINGS
                QuickSetting.Picture -> Settings.ACTION_DISPLAY_SETTINGS
                QuickSetting.Sound -> Settings.ACTION_SOUND_SETTINGS
                QuickSetting.Android -> Settings.ACTION_SETTINGS
            },
        ),
    )

    override fun setTimer(minutes: Int) {
        val works = SleepTimer.set(context, minutes)
        toast(
            when {
                minutes == 0 -> "Temporizador apagado. Tú mandas."
                works -> "La tele se apagará en $minutes min. Dulces sueños."
                else -> "Falta activar el permiso para apagar la tele (ver README)"
            },
        )
    }

    override fun setNightMode(mode: NightMode) {
        prefs.edit { putString(KEY_NIGHT, mode.name) }
        _nightMode.value = mode
        toast("Modo noche: ${mode.label}")
    }

    override fun retryWeather() {
        _weather.value = WeatherState.Loading
        startWeather()
    }

    override fun toast(message: String) {
        toastJob?.cancel()
        _toast.value = message
        toastJob = viewModelScope.launch {
            delay(2_600L)
            _toast.value = null
        }
    }

    private fun launch(intent: Intent) {
        try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: ActivityNotFoundException) {
            toast("Esta tele no tiene esa pantalla")
        } catch (_: SecurityException) {
            toast("La tele no permite abrir eso")
        }
    }

    companion object {
        private const val KEY_NIGHT = "night_mode"

        /** También la usa el protector del sistema ([CityDream]). */
        fun readNightMode(context: Context): NightMode =
            context.getSharedPreferences("cuarto", Context.MODE_PRIVATE).getString(KEY_NIGHT, null)
                ?.let { saved -> NightMode.entries.firstOrNull { it.name == saved } } ?: NightMode.Auto
    }
}

/** De 10 p. m. a 7 a. m. en automático. */
fun NightMode.isNight(hour: Int) = when (this) {
    NightMode.Always -> true
    NightMode.Never -> false
    NightMode.Auto -> hour >= 22 || hour < 7
}
