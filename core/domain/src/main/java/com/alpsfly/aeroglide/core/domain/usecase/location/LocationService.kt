package com.alpsfly.aeroglide.core.domain.usecase.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.alpsfly.aeroglide.core.data.SensorRepository
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForeground(LOCATION_NOTIFICATION_ID, buildNotification())
            }

            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val channelId = AEROGLIDE_CHANNEL_ID
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(channelId, "Recording", NotificationManager.IMPORTANCE_LOW)
        )
        return NotificationCompat.Builder(this, channelId)
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
        sensorRepository.enableRecordingListeners()
        val intent = Intent(appContext, LocationService::class.java).apply {
            action = LocationService.ACTION_START
        }
        appContext.startForegroundService(intent)
    }

    fun stopRecordingService() {
        sensorRepository.disableRecordingListeners()
        val intent = Intent(appContext, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
        }
        appContext.startService(intent)
    }
}