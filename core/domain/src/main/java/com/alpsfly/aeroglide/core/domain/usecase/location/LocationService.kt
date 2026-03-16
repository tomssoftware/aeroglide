package com.alpsfly.aeroglide.core.domain.usecase.location

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import com.alpsfly.aeroglide.core.data.SensorRepository
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startServiceAsForeground()
            }

            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }

            else -> {
                // If the system restarts the service (intent is null), we must still call startForeground
                // to avoid ForegroundServiceDidNotStartInTimeException
                startServiceAsForeground()
            }
        }
        return START_STICKY
    }

    private fun startServiceAsForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                LOCATION_NOTIFICATION_ID,
                buildNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(LOCATION_NOTIFICATION_ID, buildNotification())
        }
    }

    private fun createNotificationChannel() {
        val channelId = AEROGLIDE_CHANNEL_ID
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(channelId) == null) {
            manager.createNotificationChannel(
                NotificationChannel(channelId, "Recording", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, AEROGLIDE_CHANNEL_ID)
            .setContentTitle("AeroGlide Recording")
            .setContentText("Tracking flight data ...")
            .setSmallIcon(com.alpsfly.aeroglide.core.domain.R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("LocationService destroyed.")
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val AEROGLIDE_CHANNEL_ID = "AEROGLIDE_CHANNEL_ID"
        const val LOCATION_NOTIFICATION_ID = 1
    }
}

class ServiceStarter @Inject constructor(
    private val appContext: Context,
    private val sensorRepository: SensorRepository
) {
    fun startRecordingService() {
        if (checkSelfPermission(appContext, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            sensorRepository.enableRecordingListeners()
            val intent = Intent(appContext, LocationService::class.java).apply {
                action = LocationService.ACTION_START
            }
            Timber.i("ServiceStarter: Starting ForegroundService.")
            appContext.startForegroundService(intent)
        }
    }

    fun stopRecordingService() {
        sensorRepository.disableRecordingListeners()
        val intent = Intent(appContext, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
        }
        Timber.i("ServiceStarter: Stopping ForegroundService.")
        appContext.startService(intent)
    }
}
