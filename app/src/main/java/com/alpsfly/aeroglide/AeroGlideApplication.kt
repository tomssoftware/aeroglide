package com.alpsfly.aeroglide

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltAndroidApp
class AeroGlideApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            if (BuildConfig.DEBUG) {
                Timber.plant(Timber.DebugTree())
            }
        }
    }
}

private val applicationScope = CoroutineScope(Dispatchers.Default)
