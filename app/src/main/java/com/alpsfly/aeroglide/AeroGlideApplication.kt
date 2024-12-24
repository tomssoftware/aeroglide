package com.alpsfly.aeroglide

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.alpsfly.aeroglide.core.data.database.LocalDataSource
import com.alpsfly.aeroglide.core.data.network.WebDataSource
import com.alpsfly.aeroglide.core.data.network.firebase.CloudFunctions
import com.alpsfly.aeroglide.core.data.network.firebase.CloudStorage
import com.alpsfly.aeroglide.core.data.network.mapbox.MapStorage
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltAndroidApp
class AeroGlideApplication(val databaseName: String = "aeroglide_database") : Application() {
    private val localStorage by lazy {
        AeroGlideDatabase.getDatabase(this, databaseName)
    }

    private val mapStorage: MapStorage
        get() = MapStorage.getInstance()

    private val cloudStorage: CloudStorage
        get() = CloudStorage.getInstance()

    private val cloudFunctions: CloudFunctions
        get() = CloudFunctions.getInstance()

    private val webDataSource: WebDataSource
        get() = WebDataSource.getInstance(cloudStorage, cloudFunctions, mapStorage)

    private val localDataSource: LocalDataSource
        get() = LocalDataSource.getInstance(this, localStorage)

    val repository: AeroGlideRepository
        get() = AeroGlideRepository.getInstance(localDataSource, webDataSource)

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            if (BuildConfig.DEBUG) {
                Timber.plant(Timber.DebugTree())
            }
        }

        val channel = NotificationChannel(
            "location",
            "Location",
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

private val applicationScope = CoroutineScope(Dispatchers.Default)
