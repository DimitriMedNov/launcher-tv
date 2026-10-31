package com.dimitrimednov.launchertv.ui.cuarto

import android.service.dreams.DreamService
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.dimitrimednov.launchertv.data.Weather
import com.dimitrimednov.launchertv.data.WeatherRepository
import com.dimitrimednov.launchertv.ui.home.formatLongDate
import com.dimitrimednov.launchertv.ui.home.rememberNow
import com.dimitrimednov.launchertv.ui.theme.DesignCanvas
import java.time.ZoneId

/**
 * Protector de pantalla del sistema: el mismo de la ciudad, pero sale aunque se esté en otra app.
 * Se elige por adb (`screensaver_components`, ver README). Cualquier tecla lo cierra.
 */
class CityDream : DreamService(), LifecycleOwner, SavedStateRegistryOwner {
    private val registry = LifecycleRegistry(this)
    private val saved = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = registry
    override val savedStateRegistry: SavedStateRegistry get() = saved.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        saved.performRestore(null)
        registry.currentState = Lifecycle.State.CREATED
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false
        isFullscreen = true
        val nightMode = CityViewModel.readNightMode(this)
        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@CityDream)
            setViewTreeSavedStateRegistryOwner(this@CityDream)
            setContent {
                val now = rememberNow()
                var weather by remember { mutableStateOf<Weather?>(null) }
                LaunchedEffect(Unit) { weather = runCatching { WeatherRepository().fetch() }.getOrNull() }
                DesignCanvas(fontFamily = Barlow) {
                    CityTheme(nightMode.isNight(now.hour)) {
                        Box(Modifier.fillMaxSize()) {
                            CitySaver(
                                clockOf(now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                                formatLongDate(now).replace(",", ""),
                                weatherLong(weather, online = true),
                            )
                            Storm(dim = true)
                        }
                    }
                }
            }
        }
        setContentView(view)
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        registry.currentState = Lifecycle.State.RESUMED
    }

    override fun onDreamingStopped() {
        registry.currentState = Lifecycle.State.CREATED
        super.onDreamingStopped()
    }

    override fun onDetachedFromWindow() {
        registry.currentState = Lifecycle.State.DESTROYED
        super.onDetachedFromWindow()
    }
}
