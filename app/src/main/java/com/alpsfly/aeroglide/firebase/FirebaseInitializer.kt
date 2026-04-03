package com.alpsfly.aeroglide.firebase

import android.content.Context
import com.alpsfly.aeroglide.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseInitializer @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    fun initialize() {
        if (FirebaseApp.getApps(context).isNotEmpty()) {
            // Already initialized (likely by Google Services plugin)
            if (!BuildConfig.DEBUG) {
                // In production, we still want to install App Check provider if it's not already done.
                FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                )
                return
            }
            // In DEBUG, if it was already initialized with default options, we might need to
            // re-initialize or just configure the emulators.
            // However, calling initializeApp again with the same name [DEFAULT] throws.
        }

        if (BuildConfig.DEBUG) {
            // For local development, we use a "demo-" project ID.
            // This allows the Firebase Emulator to work without any real Firebase project setup.
            val options = FirebaseOptions.Builder()
                .setProjectId("demo-aeroglide")
                .setApplicationId("com.alpsfly.aeroglide") // Should match your packageName
                .setApiKey("fake-api-key-for-emulator")
                .build()

            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context, options)
            }

            val auth = Firebase.auth
            val firestore = Firebase.firestore
            val appCheck = FirebaseAppCheck.getInstance()

            // Point to Emulator
            auth.useEmulator(
                BuildConfig.FIREBASE_EMULATOR_HOST_ADDRESS,
                BuildConfig.FIREBASE_EMULATOR_PORT_AUTH
            )
            // Disable App Verification (reCAPTCHA/Play Integrity) for testing
            auth.firebaseAuthSettings.setAppVerificationDisabledForTesting(true)

            appCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )

            firestore.useEmulator(
                BuildConfig.FIREBASE_EMULATOR_HOST_ADDRESS,
                BuildConfig.FIREBASE_EMULATOR_PORT_FIRESTORE
            )
            firestore.firestoreSettings = firestoreSettings {
                setLocalCacheSettings(memoryCacheSettings {})
            }
        } else {
            // Production init (uses google-services.json)
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }
    }
}
