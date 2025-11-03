package com.alpsfly.aeroglide.core.hardware

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.location.OnNmeaMessageListener
import android.os.Handler
import android.os.Looper
import com.google.android.gms.location.LocationAvailability
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
import timber.log.Timber
import java.util.concurrent.Executors

@SuppressLint("MissingPermission")
fun LocationManager.locationDataFlow(
    context: Context,
    enable: Flow<Boolean>,
    interval: Long
) = callbackFlow {
    val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    if (!context.hasLocationPermission()) {
        Timber.w("Location permission is not granted")
    }
    val isGpsEnabled = isProviderEnabled(LocationManager.GPS_PROVIDER)
    val isNetworkEnabled = isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    if (!isGpsEnabled && !isNetworkEnabled) {
        Timber.w("GPS and network providers are disabled")
    }

    val request = LocationRequest.Builder(interval)
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .setIntervalMillis(1000)
        .build()
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            result.locations.lastOrNull()?.let { location ->
                this@callbackFlow.trySend(location)
            }
        }

        override fun onLocationAvailability(locationAvailability: LocationAvailability) {
            super.onLocationAvailability(locationAvailability)
            if (!locationAvailability.isLocationAvailable) {
                Timber.w("Location is not available")
            }
        }
    }

    val job = enable.onEach { enabled ->
        if (enabled) {
            fusedLocationProviderClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
            Timber.i("Location updates enabled")

        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            Timber.i("Location updates disabled")
        }
    }.launchIn(this)

    awaitClose {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        job.cancel()
    }
}

@SuppressLint("MissingPermission")
fun LocationManager.geoidCorrectionFlow(
    context: Context,
    enable: Flow<Boolean>,
    interval: Long
): Flow<Float> = callbackFlow {

    if (!context.hasLocationPermission()) {
        Timber.w("NMEA location permission is not granted")
    }
    val isGpsEnabled = isProviderEnabled(LocationManager.GPS_PROVIDER)
    val isNetworkEnabled = isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    if (!isGpsEnabled && !isNetworkEnabled) {
        Timber.w("GPS and network providers are disabled")
    }

    val handler = Handler(Looper.getMainLooper())
    val executor = Executors.newSingleThreadExecutor()

    val listener = OnNmeaMessageListener { message, _ ->
        executor.execute {
            val geoidCorrection = parseGeoidCorrection(message)
            // Only send if the value is valid
            geoidCorrection?.let {
                trySend(it)
            }
        }
    }

    val job = enable.onEach { enabled ->
        if (enabled) {
            addNmeaListener(listener, handler)
            Timber.i("NMEA location updates enabled")
        } else {
            removeNmeaListener(listener)
            Timber.i("NMEA location updates disabled")
        }
    }.launchIn(this)

    awaitClose {
        removeNmeaListener(listener)
        job.cancel()
    }
}

private fun parseGeoidCorrection(nmeaMessage: String?): Float? {
    nmeaMessage?.let { message ->
        // Check if the message is a GNGGA or GPGGA message
        if (!message.startsWith("\$GNGGA") && !message.startsWith("\$GPGGA")) {
            return null
        }

        // Check if the message has enough parts and if the geoid separation part is a valid number
        val parts = message.split(",").dropLastWhile { it.isEmpty() }
        if (parts.size >= 12) {
            Timber.d("NMEA message: $message")
            return parts[11].toFloatOrNull()
        }
    }
    return null
}
