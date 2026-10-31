package com.dimitrimednov.launchertv.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

/** Tipos de ícono del clima (los mismos de `icons.js`). */
enum class WeatherKind {
    Sunny, Partly, Cloudy, Rain, Storm, Drizzle, ClearNight, CloudyNight, Heat, Wind, Fog,
}

data class CurrentWeather(val temperature: Int, val kind: WeatherKind, val label: String)

data class DayForecast(val date: LocalDate, val kind: WeatherKind, val max: Int, val min: Int)

data class Weather(val current: CurrentWeather, val days: List<DayForecast>)

/** Clima de Progreso, Yucatán, desde Open-Meteo (gratis y sin llave). */
class WeatherRepository {

    suspend fun fetch(): Weather = withContext(Dispatchers.IO) {
        val connection = URL(URL_FORECAST).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            parse(JSONObject(connection.inputStream.bufferedReader().use { it.readText() }))
        } finally {
            connection.disconnect()
        }
    }

    private fun parse(json: JSONObject): Weather {
        val now = json.getJSONObject("current")
        val temperature = now.getDouble("temperature_2m").toInt()
        val isDay = now.getInt("is_day") == 1
        val (kind, label) = describe(
            code = now.getInt("weather_code"),
            temperature = temperature,
            windKmh = now.getDouble("wind_speed_10m"),
            isDay = isDay,
        )
        val daily = json.getJSONObject("daily")
        val dates = daily.getJSONArray("time")
        val days = (0 until dates.length()).map { i ->
            DayForecast(
                date = LocalDate.parse(dates.getString(i)),
                kind = describe(daily.getJSONArray("weather_code").getInt(i), 0, 0.0, true).first,
                max = daily.getJSONArray("temperature_2m_max").getDouble(i).toInt(),
                min = daily.getJSONArray("temperature_2m_min").getDouble(i).toInt(),
            )
        }
        return Weather(CurrentWeather(temperature, kind, label), days)
    }

    /** Pasa el código WMO de Open-Meteo al ícono y al texto en español. */
    private fun describe(code: Int, temperature: Int, windKmh: Double, isDay: Boolean): Pair<WeatherKind, String> =
        when {
            code in 95..99 -> WeatherKind.Storm to "Tormenta"
            code in 61..67 || code in 80..82 -> WeatherKind.Rain to "Lluvia"
            code in 51..57 -> WeatherKind.Drizzle to "Llovizna"
            code == 45 || code == 48 -> WeatherKind.Fog to "Neblina"
            windKmh >= WINDY_KMH -> WeatherKind.Wind to "Viento"
            temperature >= HEAT_C -> WeatherKind.Heat to "Calor extremo"
            code == 3 || code in 71..77 || code in 85..86 -> WeatherKind.Cloudy to "Nublado"
            code in 1..2 -> (if (isDay) WeatherKind.Partly else WeatherKind.CloudyNight) to "Parcialmente nublado"
            else -> if (isDay) WeatherKind.Sunny to "Cielo claro" else WeatherKind.ClearNight to "Despejado"
        }

    private companion object {
        const val HEAT_C = 36
        const val WINDY_KMH = 40.0
        const val URL_FORECAST = "https://api.open-meteo.com/v1/forecast" +
            "?latitude=21.2822136&longitude=-89.6636637" +
            "&current=temperature_2m,weather_code,is_day,wind_speed_10m" +
            "&daily=weather_code,temperature_2m_max,temperature_2m_min" +
            "&timezone=America%2FMerida&forecast_days=5"
    }
}
