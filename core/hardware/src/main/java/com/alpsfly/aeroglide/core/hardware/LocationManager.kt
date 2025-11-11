package com.alpsfly.aeroglide.core.hardware

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import android.location.OnNmeaMessageListener
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import java.util.concurrent.Executors

@SuppressLint("MissingPermission")
fun LocationManager.locationDataFlow(
    context: Context,
    enable: Flow<Boolean>,
    interval: Long
) = callbackFlow {
    if (!context.hasLocationPermission()) {
        Timber.w("Location permission is not granted")
    }
    val isGpsEnabled = isProviderEnabled(LocationManager.GPS_PROVIDER)
    val isNetworkEnabled = isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    if (!isGpsEnabled && !isNetworkEnabled) {
        Timber.w("GPS and network providers are disabled")
    }
    val provider = LocationManager.GPS_PROVIDER

    val listener = LocationListener { location ->
        Timber.d("GPS_PROVIDER new location: ${location.latitude}, ${location.longitude}")
        trySend(location)
    }

    enable.collectLatest { isEnabled ->
        if (isEnabled) {
            requestLocationUpdates(
                provider,
                interval,
                0f,
                listener,
                Looper.getMainLooper()
            )
        } else {
            removeUpdates(listener)
        }
    }

    awaitClose {
        removeUpdates(listener)
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
                    Timber.d("NMEA message: $message")
                }
            }
        }
    }

    // We now collect the 'enable' flow directly.
    enable.collectLatest { isEnabled ->
        if (isEnabled) {
            Timber.i("NMEA location updates enabled")
            addNmeaListener(listener, handler)
        } else {
            Timber.i("NMEA location updates disabled")
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
