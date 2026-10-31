package com.dimitrimednov.launchertv.data

import android.content.ComponentName
import androidx.compose.ui.graphics.Color
import java.time.LocalDate

/**
 * Los mismos datos de ejemplo del diseño de Claude Design (`APPS`, `INPUTS`, clima 27° Cielo claro).
 * Sirven para comparar la app contra el diseño y sacar capturas en el emulador.
 * Solo se activan abriendo la app con `adb shell am start -n …/.MainActivity --ez demo true`.
 */
object DemoData {
    private const val SELF = "com.dimitrimednov.launchertv"

    private fun app(packageName: String, name: String, brand: Brand) =
        LauncherApp(packageName, name, ComponentName(SELF, "$SELF.MainActivity"), brand)

    val apps: AppList by lazy {
        val known = KnownApps.associateBy { it.packageName }
        val fox = KnownApp("demo.foxone", "FOX One", Brand(listOf(Color(0xFF1A1A1A)), Color.White, Color(0xFFC7C7CC)))
        val order = KnownApps.map { it.packageName }.toMutableList().apply {
            add(indexOf("tv.twitch.android.app"), fox.packageName)
        }
        AppList(
            visible = order.map { pkg -> (known[pkg] ?: fox).let { app(it.packageName, it.name, it.brand) } },
            hidden = emptyList(),
        )
    }

    val inputs = listOf(
        TvInput("demo/HW2", 1, InputKind.Console, "Consola", hasSignal = true),
        TvInput("demo/HW3", 2, InputKind.SetTopBox, "Decodificador", hasSignal = true),
        TvInput("demo/HW4", 3, InputKind.Hdmi, "HDMI 3", hasSignal = false),
        TvInput("demo/HW5", 4, InputKind.Laptop, "Computadora", hasSignal = false),
        TvInput("demo/HW0", null, InputKind.Antenna, "Antena", hasSignal = true),
    )

    private fun playing(pkg: String, title: String, subtitle: String?, album: String?, position: Long, duration: Long, isPlaying: Boolean = false) =
        NowPlaying(pkg, title, subtitle, album, position, duration, isPlaying)

    private const val MIN = 60_000L

    val nowPlaying: Map<String, NowPlaying> = listOf(
        playing(LIVE_TV_PACKAGE, "Noticias del mediodía", "Canal 5 · 1:00 – 2:00 p. m.", null, 36 * MIN, 60 * MIN),
        playing("com.apple.atve.androidtv.appletv", "Silencio en la costa", "T1 · E4", null, 20 * MIN, 58 * MIN),
        playing("com.netflix.ninja", "La casa del mar", "T2 · E3", null, 22 * MIN, 53 * MIN),
        playing("com.disney.disneyplus", "Expedición Ártica", "E2", null, 33 * MIN, 55 * MIN),
        playing("com.amazon.amazonvideo.livingroom", "Puerto Progreso", "T1 · E1", null, 7 * MIN, 57 * MIN),
        playing(SPOTIFY_PACKAGE, "Oye cómo va", "Santana", "Abraxas", 125_000, 257_000, isPlaying = true),
        playing("com.amco.clarovideo_atv", "Tierra de henequén", "Película", null, 18 * MIN, 90 * MIN),
        playing("com.wbd.stream", "Crónicas de la península", "T1 · E2", null, 19 * MIN, 63 * MIN),
        playing("com.univision.prendetv", "El faro de Celestún", "Capítulo 18", null, 31 * MIN, 56 * MIN),
    ).associateBy { it.packageName }

    val weather: Weather by lazy {
        val today = LocalDate.now()
        Weather(
            current = CurrentWeather(27, WeatherKind.Sunny, "Cielo claro"),
            days = listOf(
                DayForecast(today, WeatherKind.Partly, 31, 24),
                DayForecast(today.plusDays(1), WeatherKind.Rain, 29, 23),
                DayForecast(today.plusDays(2), WeatherKind.Storm, 28, 23),
            ),
        )
    }
}
