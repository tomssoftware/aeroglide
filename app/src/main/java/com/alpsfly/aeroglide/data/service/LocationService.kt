package com.alpsfly.aeroglide.data.service

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventCallback
import android.hardware.SensorManager
import android.location.LocationManager
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.alpsfly.aeroglide.R
import com.alpsfly.aeroglide.data.repository.SensorRepository
import com.alpsfly.aeroglide.data.util.SensorData
import com.alpsfly.aeroglide.data.util.SensorType
import com.alpsfly.aeroglide.data.util.accelSensorDataFlow
import com.alpsfly.aeroglide.data.util.locationDataFlow
import com.alpsfly.aeroglide.data.util.pressureSensorDataFlow
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Inject
    lateinit var sensorRepository: SensorRepository

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        serviceScope.launch {
            sensorManager.accelSensorDataFlow().collect {
                sensorRepository.updateAcceleration(it)
            }
        }

        serviceScope.launch {
            sensorManager.pressureSensorDataFlow().collect {
                sensorRepository.updatePressure(it)
            }
        }

        serviceScope.launch {
            locationManager.locationDataFlow(this@LocationService, 1000L).collect {
                sensorRepository.updateLocation(it)
            }
        }
    }

    private fun start() {
        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("Tracking location...")
            .setContentText("Location: null")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        locationManager.locationDataFlow(applicationContext, 1000L)
//            .catch { e -> e.printStackTrace() }
//            .onEach { location ->
//                val lat = location.latitude.toString().takeLast(3)
//                val long = location.longitude.toString().takeLast(3)
//                val updatedNotification = notification.setContentText("Location: ($lat, $long)")
//                notificationManager.notify(1, updatedNotification.build())
//            }.launchIn(serviceScope)
        startForeground(1, notification.build())
    }

    private fun stop() {
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}