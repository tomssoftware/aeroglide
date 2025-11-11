package com.alpsfly.aeroglide

import android.app.Application
import android.util.Log
import com.alpsfly.aeroglide.core.common.di.ApplicationScope
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.auth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class AeroGlideApplication : Application() {
    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    inner class CrashlyticsTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            when (priority) {
                Log.ERROR, Log.WARN -> {
                    if (t == null) {
                        FirebaseCrashlytics.getInstance().recordException(Exception(message))
                    } else {
                        FirebaseCrashlytics.getInstance().recordException(t)
                    }
                }

                Log.INFO -> {
                    FirebaseCrashlytics.getInstance().log(message)
                }

                Log.ASSERT, Log.VERBOSE, Log.DEBUG -> {
                    return
                }
            }
        }
    }


    // private val localStorage by lazy { database }

//    private val mapStorage: com.alpsfly.aeroglide.core.mapbox.data.MapStorage
//        get() = com.alpsfly.aeroglide.core.mapbox.data.MapStorage.getInstance()

//    private val cloudStorage: CloudStorage
//        get() = CloudStorage.getInstance()
//
//    private val cloudFunctions: com.alpsfly.aeroglide.core.firebase.CloudFunctions
//        get() = com.alpsfly.aeroglide.core.firebase.CloudFunctions.getInstance()

//    private val webDataSource: WebDataSource
//        get() = WebDataSource.getInstance(cloudStorage, cloudFunctions, mapStorage)
//
//    private val localDataSource: LocalDataSource
//        get() = LocalDataSource.getInstance(this, localStorage)
//
//    val repository: AeroGlideRepository
//        get() = AeroGlideRepository.getInstance(localDataSource, webDataSource)

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        val firestore = Firebase.firestore
        val auth = Firebase.auth

        if (BuildConfig.DEBUG) {
            val firebaseAppCheck = FirebaseAppCheck.getInstance()
            firebaseAppCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
            firestore.useEmulator(
                BuildConfig.FIREBASE_EMULATOR_HOST_ADDRESS,
                BuildConfig.FIREBASE_EMULATOR_PORT_FIRESTORE
            )
            firestore.firestoreSettings = firestoreSettings {
                isPersistenceEnabled = false
            }
            auth.useEmulator(
                BuildConfig.FIREBASE_EMULATOR_HOST_ADDRESS,
                BuildConfig.FIREBASE_EMULATOR_PORT_AUTH
            )
        } else {
            val firebaseAppCheck = FirebaseAppCheck.getInstance()
            firebaseAppCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance())
        }

        applicationScope.launch {
            if (BuildConfig.DEBUG) {
                Timber.plant(Timber.DebugTree())
            }
            Timber.plant(CrashlyticsTree())
        }
    }
}

