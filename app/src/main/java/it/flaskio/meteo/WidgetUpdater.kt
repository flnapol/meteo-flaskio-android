package it.flaskio.meteo
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import kotlinx.coroutines.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object WidgetUpdater {
    fun update(context:Context, manager:AppWidgetManager, ids:IntArray, layout:Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val location = LocationStore.load(context)
            val d = if (location != null) try { WeatherRepository.fetch(location) } catch (_:Exception) { null } else null
            withContext(Dispatchers.Main) {
                ids.forEach { id ->
                    val v=RemoteViews(context.packageName,layout)
                    val pi=PendingIntent.getActivity(context,0,Intent(context,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE)
                    v.setOnClickPendingIntent(R.id.widget_root,pi)
                    if(d!=null) {
                        setIfExists(v, layout, R.id.location, location!!.name)
                        v.setTextViewText(R.id.temp,"${d.temperature}°C")
                        v.setTextViewText(R.id.condition,"${WeatherRepository.symbol(d.weatherCode)} ${WeatherRepository.description(d.weatherCode)}")
                        v.setTextViewText(R.id.minmax,"↑ ${d.max}°   ↓ ${d.min}°")
                        setIfExists(v, layout, R.id.details,"💧 ${d.humidity}%   🌬 ${d.wind} km/h ${d.windDir}")
                        setIfExists(v, layout, R.id.air,"Aria: ${WeatherRepository.airText(d.aqi)}${d.aqi?.let{" · AQI $it"}?:""}")
                        setIfExists(v, layout, R.id.hail,"Grandine: rischio ${d.hailRisk}")
                        val forecast=d.days.joinToString("     "){"${it.label} ${WeatherRepository.symbol(it.code)} ${it.min}°/${it.max}°"}
                        setIfExists(v, layout, R.id.forecast,forecast)
                        setIfExists(v, layout, R.id.updated,"Agg. "+LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")))
                    } else {
                        setIfExists(v, layout, R.id.location, "Seleziona una località"); v.setTextViewText(R.id.condition,"Tocca per scegliere")
                    }
                    manager.updateAppWidget(id,v)
                }
            }
        }
    }
    private fun setIfExists(v:RemoteViews, layout:Int, id:Int, text:String) {
        try { v.setTextViewText(id,text) } catch (_:Exception) {}
    }
}
