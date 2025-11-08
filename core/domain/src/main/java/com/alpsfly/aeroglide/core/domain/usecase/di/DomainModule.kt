package com.alpsfly.aeroglide.core.domain.usecase.di

import android.app.Application
import android.content.Context
import com.alpsfly.aeroglide.core.common.di.ApplicationScope
import com.alpsfly.aeroglide.core.data.AppRepository
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.domain.usecase.CalibrationProcessor
import com.alpsfly.aeroglide.core.domain.usecase.CalibrationUseCase
import com.alpsfly.aeroglide.core.domain.usecase.FlightSessionCoordinatorUseCase
import com.alpsfly.aeroglide.core.domain.usecase.RecordingUseCase
import com.alpsfly.aeroglide.core.domain.usecase.location.ServiceStarter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
     * This provider belongs more in a hardware-specific module,
     * but it's acceptable here for simplicity.
     */
    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(app: Application): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(app)
    }

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
        return CalibrationProcessor(appRepository, sensorRepository, applicationScope)
    }

    /**
     * Provides the "Manager" use case for calibration.
     * This can be a regular (non-singleton) provider as it is lightweight and stateless.
     */
    @Provides
    fun provideCalibrationUseCase(
        appRepository: AppRepository,
        sensorRepository: SensorRepository,
        calibrationProcessor: CalibrationProcessor,
        serviceStarter: ServiceStarter
    ): CalibrationUseCase {
        return CalibrationUseCase(
            appRepository,
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
        appRepository: AppRepository,
        calibrationUseCase: CalibrationUseCase,
        recordingUseCase: RecordingUseCase,
        @ApplicationScope applicationScope: CoroutineScope
    ): FlightSessionCoordinatorUseCase {
        return FlightSessionCoordinatorUseCase(
            appRepository,
            calibrationUseCase,
            recordingUseCase,
            applicationScope
        )
    }

    // You will add providers for RecordActivityUseCase, RecordingProcessor,
    // AutoStartUseCase, AutoStartProcessor, etc., here in the future.
}
