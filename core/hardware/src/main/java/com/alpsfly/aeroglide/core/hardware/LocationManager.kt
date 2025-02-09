package com.alpsfly.aeroglide.core.hardware

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LocationException(message: String) : Exception(message)

@SuppressLint("MissingPermission")
fun LocationManager.locationDataFlow(
    context: Context,
    enableLocationUpdates: Flow<Boolean>,
    interval: Long
) = callbackFlow {
    val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    if (!context.hasLocationPermission()) {
        throw LocationException("Missing location permission")
    }
    val isGpsEnabled = isProviderEnabled(LocationManager.GPS_PROVIDER)
    val isNetworkEnabled = isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    if (!isGpsEnabled && !isNetworkEnabled) {
        throw LocationException("GPS is disabled")
    }
    val request = LocationRequest.Builder(interval)
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .build()
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            result.locations.lastOrNull()?.let { location ->
                this@callbackFlow.trySend(location)
            }
        }
    }

    val job = enableLocationUpdates.onEach { enabled ->
        if (enabled) {
            fusedLocationProviderClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            // todo: check if implementation is correct
            getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)?.let { it ->
                it.speed = 0f
                this@callbackFlow.trySend(it)
            }
         }
    }.launchIn(this)

    awaitClose {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        job.cancel()
    }
}
