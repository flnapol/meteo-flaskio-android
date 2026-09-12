package it.flaskio.meteo

import org.json.JSONObject
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class WeatherData(
    val temperature: Int,
    val apparent: Int,
    val humidity: Int,
    val wind: Int,
    val windDir: String,
    val weatherCode: Int,
    val max: Int,
    val min: Int,
    val precipitationProbability: Int,
    val aqi: Int?,
    val pm25: Double?,
    val hailRisk: String,
    val days: List<ForecastDay>
)
data class ForecastDay(val label:String, val max:Int, val min:Int, val code:Int)

object WeatherRepository {

    fun fetch(location: UserLocation): WeatherData {
        val LAT = location.latitude
        val LON = location.longitude
        val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=$LAT&longitude=$LON" +
            "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m,wind_direction_10m" +
            "&hourly=precipitation_probability,cape&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max" +
            "&timezone=Europe%2FRome&forecast_days=7"
        val w = JSONObject(URL(weatherUrl).readText())
        val current = w.getJSONObject("current")
        val daily = w.getJSONObject("daily")
        val hourly = w.getJSONObject("hourly")

        val temp = current.getDouble("temperature_2m").toInt()
        val apparent = current.getDouble("apparent_temperature").toInt()
        val humidity = current.getInt("relative_humidity_2m")
        val wind = current.getDouble("wind_speed_10m").toInt()
        val windDir = compass(current.getDouble("wind_direction_10m"))
        val code = current.getInt("weather_code")
        val max = daily.getJSONArray("temperature_2m_max").getDouble(0).toInt()
        val min = daily.getJSONArray("temperature_2m_min").getDouble(0).toInt()
        val pop = daily.getJSONArray("precipitation_probability_max").getInt(0)

        // Heuristic hail-risk signal: thunderstorms + CAPE / precipitation probability.
        val capeArr = hourly.getJSONArray("cape")
        var maxCape = 0.0
        for (i in 0 until minOf(24, capeArr.length())) {
            if (!capeArr.isNull(i)) maxCape = maxOf(maxCape, capeArr.getDouble(i))
        }
        val hail = when {
            code in listOf(96,99) || (maxCape >= 1200 && pop >= 60) -> "ALTO"
            code in listOf(95) || (maxCape >= 700 && pop >= 40) -> "MODERATO"
            else -> "BASSO"
        }

        val dates = daily.getJSONArray("time")
        val maxs = daily.getJSONArray("temperature_2m_max")
        val mins = daily.getJSONArray("temperature_2m_min")
        val codes = daily.getJSONArray("weather_code")
        val fmt = DateTimeFormatter.ofPattern("EEE")
        val days = (0 until minOf(5, dates.length())).map { i ->
            ForecastDay(
                LocalDate.parse(dates.getString(i)).format(fmt).replaceFirstChar { it.uppercase() },
                maxs.getDouble(i).toInt(), mins.getDouble(i).toInt(), codes.getInt(i)
            )
        }

        var aqi:Int? = null
        var pm25:Double? = null
        try {
            val aqUrl = "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=$LAT&longitude=$LON&current=european_aqi,pm2_5&timezone=Europe%2FRome"
            val aq = JSONObject(URL(aqUrl).readText()).getJSONObject("current")
            if (!aq.isNull("european_aqi")) aqi = aq.getDouble("european_aqi").toInt()
            if (!aq.isNull("pm2_5")) pm25 = aq.getDouble("pm2_5")
        } catch (_:Exception) {}

        return WeatherData(temp, apparent, humidity, wind, windDir, code, max, min, pop, aqi, pm25, hail, days)
    }

    fun description(code:Int) = when(code) {
        0 -> "Sereno"
        1 -> "Prevalentemente sereno"
        2 -> "Poco nuvoloso"
        3 -> "Nuvoloso"
        45,48 -> "Nebbia"
        51,53,55,56,57 -> "Pioviggine"
        61,63,65,66,67,80,81,82 -> "Pioggia"
        71,73,75,77,85,86 -> "Neve"
        95,96,99 -> "Temporale"
        else -> "Variabile"
    }
    fun symbol(code:Int) = when(code) {
        0 -> "☀️"; 1,2 -> "🌤️"; 3 -> "☁️"; 45,48 -> "🌫️"
        51,53,55,56,57,61,63,65,66,67,80,81,82 -> "🌧️"
        71,73,75,77,85,86 -> "❄️"; 95,96,99 -> "⛈️"; else -> "🌤️"
    }
    fun airText(aqi:Int?) = when {
        aqi == null -> "n/d"
        aqi <= 20 -> "Ottima"
        aqi <= 40 -> "Buona"
        aqi <= 60 -> "Moderata"
        aqi <= 80 -> "Scarsa"
        aqi <= 100 -> "Molto scarsa"
        else -> "Estremamente scarsa"
    }
    private fun compass(d:Double):String {
        val dirs = arrayOf("N","NE","E","SE","S","SO","O","NO")
        return dirs[((d/45.0)+0.5).toInt()%8]
    }
}
