package de.tomssoftware.aeroglide

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import de.tomssoftware.aeroglide.core.common.di.ApplicationScope
import de.tomssoftware.aeroglide.firebase.CrashlyticsTree
import de.tomssoftware.aeroglide.firebase.FirebaseInitializer
import de.tomssoftware.aeroglide.sync.initializers.Sync
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class AeroGlideApplication : Application(), Configuration.Provider {

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var firebaseInitializer: FirebaseInitializer

    @Inject
    lateinit var crashlyticsTree: CrashlyticsTree

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    /** Provides a custom WorkManager configuration that uses Hilt's worker factory. */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        firebaseInitializer.initialize()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.plant(crashlyticsTree)

        // Start the upload worker once on app launch; WorkManager deduplicates via KEEP policy.
        Sync.initialize(this)
    }
}

