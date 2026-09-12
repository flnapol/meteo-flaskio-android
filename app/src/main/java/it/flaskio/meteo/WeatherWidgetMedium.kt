package it.flaskio.meteo
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
class WeatherWidgetMedium: AppWidgetProvider() {
 override fun onUpdate(context:Context, manager:AppWidgetManager, ids:IntArray) {
  WidgetUpdater.update(context,manager,ids,R.layout.widget_medium)
 }
}
