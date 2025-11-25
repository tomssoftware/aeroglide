package com.alpsfly.aeroglide

import android.app.Application
import com.alpsfly.aeroglide.core.common.di.ApplicationScope
import com.alpsfly.aeroglide.firebase.CrashlyticsTree
import com.alpsfly.aeroglide.firebase.FirebaseInitializer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class AeroGlideApplication : Application() {
    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var firebaseInitializer: FirebaseInitializer

    @Inject
    lateinit var crashlyticsTree: CrashlyticsTree

    override fun onCreate() {
        super.onCreate()
        firebaseInitializer.initialize()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.plant(crashlyticsTree)
    }
}

