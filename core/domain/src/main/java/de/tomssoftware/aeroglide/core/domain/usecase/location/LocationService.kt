package de.tomssoftware.aeroglide.core.domain.usecase.location

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
import de.tomssoftware.aeroglide.core.data.SensorRepository
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {

    @Inject
    lateinit var sensorRepository: SensorRepository

    override fun onCreate() {
        super.onCreate()
        Timber.d("LocationService: onCreate")
        createNotificationChannel()

        // Calling it here satisfies the system's requirement for startForegroundService
        // as early as possible.
        startServiceAsForeground()
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        Timber.d("LocationService: onStartCommand action=$action startId=$startId")

        // CRITICAL: Always call startForeground first to satisfy the system's promise,
        // especially when rapid start/stop calls occur.
        // This prevents ForegroundServiceDidNotStartInTimeException if a stop follows a start immediately.
        startServiceAsForeground()

        when (action) {
            ACTION_START -> {
                sensorRepository.enableSensorListeners()
            }

            ACTION_STOP -> {
                Timber.d("LocationService: Processing ACTION_STOP")
                sensorRepository.disableSensorListeners()
                // Only stop if no newer start commands are pending.
                // If we used stopSelf() here, and a new ACTION_START was already in the queue,
                // the service would die before processing the start, causing the crash.
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf(startId)
            }

            else -> {
                // Handle sticky restarts (intent is null)
                sensorRepository.enableSensorListeners()
            }
        }
        return START_STICKY
    }

    private fun startServiceAsForeground() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                LOCATION_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION,
            )
        } else {
            startForeground(LOCATION_NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        val channelId = AEROGLIDE_CHANNEL_ID
        val manager = getSystemService(NotificationManager::class.java)
        if (manager != null && manager.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(
                channelId,
                "Flight Recording",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Keep AeroGlide running in the background during flight"
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, AEROGLIDE_CHANNEL_ID)
            .setContentTitle("AeroGlide Recording")
            .setContentText("Flight tracking is active")
            .setSmallIcon(de.tomssoftware.aeroglide.core.domain.R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        Timber.d("LocationService: onDestroy")
        sensorRepository.disableSensorListeners()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val AEROGLIDE_CHANNEL_ID = "AEROGLIDE_CHANNEL_ID"
        const val LOCATION_NOTIFICATION_ID = 1
    }
}

class LocationServiceStarter @Inject constructor(
    private val appContext: Context,
) {
    fun startForegroundService() {
        if (checkSelfPermission(appContext, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(appContext, LocationService::class.java).apply {
                action = LocationService.ACTION_START
            }
            Timber.i("ServiceStarter: Calling startForegroundService")
            appContext.startForegroundService(intent)
        }
    }

    fun stopForegroundService() {
        val intent = Intent(appContext, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
        }
        Timber.i("ServiceStarter: Sending ACTION_STOP")
        // We use startService (or startForegroundService) to deliver the intent.
        // If the service is already running, this just calls onStartCommand.
        appContext.startService(intent)
    }
}
