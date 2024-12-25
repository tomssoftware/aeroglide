package com.alpsfly.aeroglide

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import com.alpsfly.aeroglide.core.data.network.firebase.CloudFunctions
import com.alpsfly.aeroglide.core.data.network.mapbox.MapStorage
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase

import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class AeroGlideApplication @Inject constructor (/*private val database: AeroGlideDatabase*/) : Application() {



   // private val localStorage by lazy { database }

    private val mapStorage: MapStorage
        get() = MapStorage.getInstance()

//    private val cloudStorage: CloudStorage
//        get() = CloudStorage.getInstance()
//
    private val cloudFunctions: CloudFunctions
        get() = CloudFunctions.getInstance()

//    private val webDataSource: WebDataSource
//        get() = WebDataSource.getInstance(cloudStorage, cloudFunctions, mapStorage)
//
//    private val localDataSource: LocalDataSource
//        get() = LocalDataSource.getInstance(this, localStorage)
//
//    val repository: AeroGlideRepository
//        get() = AeroGlideRepository.getInstance(localDataSource, webDataSource)

    init {

    }

    override fun onCreate() {
        super.onCreate()
//        FirebaseApp.initializeApp(this)
//        val firestore = Firebase.firestore
//        val auth = Firebase.auth

        applicationScope.launch {
            if (BuildConfig.DEBUG) {
                Timber.plant(Timber.DebugTree())
            }
        }

//        if (BuildConfig.DEBUG) {
//            val firebaseAppCheck = FirebaseAppCheck.getInstance()
//            firebaseAppCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
//            firestore.useEmulator(BuildConfig.FIREBASE_EMULATOR_HOST_ADDRESS, BuildConfig.FIREBASE_EMULATOR_PORT_FIRESTORE)
//            firestore.firestoreSettings = firestoreSettings {
//                isPersistenceEnabled = false
//            }
//            auth.useEmulator(BuildConfig.FIREBASE_EMULATOR_HOST_ADDRESS, BuildConfig.FIREBASE_EMULATOR_PORT_AUTH)
//            Timber.tag("APP")
//                .i("Use firebase emulator! Host: ${BuildConfig.FIREBASE_EMULATOR_HOST_ADDRESS}")
//        } else {
//            val firebaseAppCheck = FirebaseAppCheck.getInstance()
//            firebaseAppCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance())
//            Timber.tag("APP")
//                .i("Use firebase functions! Url: ${BuildConfig.FIREBASE_FUNCTIONS_URL}")
//        }

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
