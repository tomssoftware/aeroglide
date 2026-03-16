package com.alpsfly.aeroglide.core.domain.usecase.di

import android.content.Context
import android.content.SharedPreferences
import com.alpsfly.aeroglide.core.common.audio.VarioTone
import com.alpsfly.aeroglide.core.common.di.ApplicationScope
import com.alpsfly.aeroglide.core.data.AppRepository
import com.alpsfly.aeroglide.core.data.AutoStartSettingsProvider
import com.alpsfly.aeroglide.core.data.BillingRepository
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.domain.usecase.AutoStartProcessor
import com.alpsfly.aeroglide.core.domain.usecase.AutoStartUseCase
import com.alpsfly.aeroglide.core.domain.usecase.CalibrationProcessor
import com.alpsfly.aeroglide.core.domain.usecase.CalibrationUseCase
import com.alpsfly.aeroglide.core.domain.usecase.DownloadElevationUseCase
import com.alpsfly.aeroglide.core.domain.usecase.FlightSessionCoordinatorUseCase
import com.alpsfly.aeroglide.core.domain.usecase.PurchaseUseCase
import com.alpsfly.aeroglide.core.domain.usecase.RecordingUseCase
import com.alpsfly.aeroglide.core.domain.usecase.VarioToneUseCase
import com.alpsfly.aeroglide.core.domain.usecase.location.ServiceStarter
import com.alpsfly.aeroglide.core.domain.usecase.state.AppStateManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    /**
     * Provides the ServiceStarter which is a domain-level helper
     * for interacting with the LocationService.
     */
    @Provides
    @Singleton
    fun provideServiceStarter(
        @ApplicationContext context: Context,
        sensorRepository: SensorRepository
    ): ServiceStarter {
        return ServiceStarter(context, sensorRepository)
    }

    /**
     * Provides the "Worker" processor for the calibration logic.
     * It is a singleton as it manages a long-running, stateful process.
     */
    @Provides
    @Singleton
    fun provideCalibrationProcessor(
        appRepository: AppRepository,
        sensorRepository: SensorRepository,
        // Hilt will automatically provide the @ApplicationScope CoroutineScope
        // assuming it's defined in another module (e.g., CommonModule or AppModule).
        @ApplicationScope applicationScope: CoroutineScope
    ): CalibrationProcessor {
        return CalibrationProcessor(sensorRepository, applicationScope)
    }

    /**
     * Provides the "Manager" use case for calibration.
     * This can be a regular (non-singleton) provider as it is lightweight and stateless.
     */
    @Provides
    fun provideCalibrationUseCase(
        appStateManager: AppStateManager,
        calibrationProcessor: CalibrationProcessor,
        serviceStarter: ServiceStarter
    ): CalibrationUseCase {
        return CalibrationUseCase(
            appStateManager,
            calibrationProcessor,
            serviceStarter
        )
    }

    /**
     * Provides the "Coordinator" which manages all other use cases.
     * It should be a singleton to ensure there's only one central coordinator.
     */
    @Provides
    @Singleton
    fun provideFlightSessionCoordinatorUseCase(
        appStateManager: AppStateManager,
        calibrationUseCase: CalibrationUseCase,
        recordingUseCase: RecordingUseCase,
        autoStartUseCase: AutoStartUseCase,
        downloadElevationUseCase: DownloadElevationUseCase,
        sensorRepository: SensorRepository,
        autoStartSettingsProvider: AutoStartSettingsProvider,
        @ApplicationScope applicationScope: CoroutineScope
    ): FlightSessionCoordinatorUseCase {
        return FlightSessionCoordinatorUseCase(
            appStateManager,
            calibrationUseCase,
            recordingUseCase,
            autoStartUseCase,
            downloadElevationUseCase,
            sensorRepository,
            autoStartSettingsProvider,
            applicationScope
        )
    }

    /**
     * Provides the "Worker" processor for the auto-start logic.
     * It is a singleton as it manages a long-running, stateful process.
     */
    @Provides
    @Singleton
    fun provideAutoStartProcessor(
        sensorRepository: SensorRepository,
        settingsProvider: AutoStartSettingsProvider,
        @ApplicationScope applicationScope: CoroutineScope
    ): AutoStartProcessor {
        return AutoStartProcessor(
            sensorRepository,
            settingsProvider,
            applicationScope
        )
    }

    /**
     * Provides the "Manager" use case for auto-start.
     * Singleton to ensure the init block (callback registration) only runs once.
     */
    @Provides
    @Singleton
    fun provideAutoStartUseCase(
        appStateManager: AppStateManager,
        autoStartProcessor: AutoStartProcessor,
        serviceStarter: ServiceStarter
    ): AutoStartUseCase {
        return AutoStartUseCase(
            appStateManager,
            autoStartProcessor,
            serviceStarter
        )
    }

    @Provides
    fun providePurchaseUseCase(billingRepository: BillingRepository): PurchaseUseCase {
        return PurchaseUseCase(billingRepository)
    }

    /**
     * Provides the [VarioToneUseCase] singleton that maps climb rate to audio tone parameters
     * and controls [VarioTone] playback.
     *
     * Requires the [SharedPreferences] singleton (provided by `SettingsModule`) to read the
     * user-configured climb and sink thresholds without creating a separate settings provider.
     */
    @Provides
    @Singleton
    fun provideVarioToneUseCase(
        sensorRepository: SensorRepository,
        varioTone: VarioTone,
        prefs: SharedPreferences,
        @ApplicationScope applicationScope: CoroutineScope,
    ): VarioToneUseCase = VarioToneUseCase(sensorRepository, varioTone, prefs, applicationScope)
}
