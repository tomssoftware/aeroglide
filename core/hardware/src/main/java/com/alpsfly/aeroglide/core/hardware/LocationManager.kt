package com.alpsfly.aeroglide.core.hardware

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.location.OnNmeaMessageListener
import android.os.Handler
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import java.util.concurrent.Executors

@SuppressLint("MissingPermission")
fun FusedLocationProviderClient.locationDataFlow(
    context: Context,
    enable: Flow<Boolean>,
    interval: Long
): Flow<Location> = callbackFlow {
    if (!context.hasLocationPermission()) {
        Timber.w("Location permission is not granted")
    }

    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                Timber.v("Fused location: ${location.latitude}, ${location.longitude}, ${location.altitude}, ${location.accuracy}")
                trySend(location)
            }
        }
    }

    val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval).build()

    enable.collectLatest { isEnabled ->
        if (isEnabled) {
            if (context.hasLocationPermission()) {
                requestLocationUpdates(
                    request,
                    callback,
                    Looper.getMainLooper()
                )
            } else {
                Timber.w("Location permission missing, cannot start updates")
            }
        } else {
            removeLocationUpdates(callback)
        }
    }

    awaitClose {
        removeLocationUpdates(callback)
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
            geoidCorrection?.let {
                if (trySend(it).isSuccess) {
                    Timber.v("NMEA message: $message")
                }
            }
        }
    }

    // We now collect the 'enable' flow directly.
    enable.collectLatest { isEnabled ->
        if (isEnabled) {
            Timber.d("NMEA location updates enabled")
            addNmeaListener(listener, handler)
        } else {
            Timber.d("NMEA location updates disabled")
            removeNmeaListener(listener)
        }
    }

    // The awaitClose block is now simpler, as it no longer needs to cancel the job.
    awaitClose {
        removeNmeaListener(listener)
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
            return parts[11].toFloatOrNull()
        }
    }
    return null
}
