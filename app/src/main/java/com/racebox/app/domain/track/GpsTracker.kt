package com.racebox.app.domain.track

import android.content.Context
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class GpsTracker(context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private var callback: LocationCallback? = null

    private val _locations = MutableSharedFlow<android.location.Location>(extraBufferCapacity = 64)
    val locations: SharedFlow<android.location.Location> = _locations.asSharedFlow()

    fun start() {
        stop()
        val newCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.lastOrNull()?.let { _locations.tryEmit(it) }
            }
        }
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(UPDATE_INTERVAL_MS)
            .setMaxUpdateDelayMillis(MAX_DELAY_MS)
            .build()
        fusedLocationClient.requestLocationUpdates(request, newCallback, Looper.getMainLooper())
        callback = newCallback
    }

    fun stop() {
        callback?.let { fusedLocationClient.removeLocationUpdates(it) }
        callback = null
    }

    fun isProviderEnabled(): Boolean =
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    private companion object {
        const val UPDATE_INTERVAL_MS = 1000L
        const val MAX_DELAY_MS = 2000L
    }
}