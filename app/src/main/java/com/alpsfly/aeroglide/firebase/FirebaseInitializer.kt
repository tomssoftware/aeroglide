package com.alpsfly.aeroglide.firebase

import android.content.Context
import com.alpsfly.aeroglide.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseInitializer @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    fun initialize() {
        FirebaseApp.initializeApp(context)
        val firestore = Firebase.firestore
        val auth = Firebase.auth

        if (BuildConfig.DEBUG) {
            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
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
            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }
    }
}
