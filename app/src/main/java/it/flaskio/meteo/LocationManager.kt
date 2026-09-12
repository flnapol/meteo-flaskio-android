package it.flaskio.meteo

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object LocationManager {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): UserLocation? =
        suspendCancellableCoroutine { cont ->

            val client = LocationServices.getFusedLocationProviderClient(context)

            client.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                null
            ).addOnSuccessListener { location ->
                if (cont.isActive) {
                    if (location != null) {
                        cont.resume(
                            UserLocation(
                                name = "Posizione attuale",
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                        )
                    } else {
                        cont.resume(null)
                    }
                }
            }.addOnFailureListener {
                if (cont.isActive) cont.resume(null)
            }
        }
}
