package it.flaskio.meteo

import android.content.Context

object LocationStore {
    private const val PREFS = "meteo_flaskio_location"

    fun save(context: Context, location: UserLocation) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString("name", location.name)
            .putString("latitude", location.latitude.toString())
            .putString("longitude", location.longitude.toString())
            .apply()
    }

    fun load(context: Context): UserLocation? {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val name = p.getString("name", null) ?: return null
        val lat = p.getString("latitude", null)?.toDoubleOrNull() ?: return null
        val lon = p.getString("longitude", null)?.toDoubleOrNull() ?: return null
        return UserLocation(name, lat, lon)
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }
}
