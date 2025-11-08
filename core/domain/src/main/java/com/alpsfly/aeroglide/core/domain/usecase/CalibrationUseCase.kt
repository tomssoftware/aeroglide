package com.alpsfly.aeroglide.core.domain.usecase

import com.alpsfly.aeroglide.core.data.AppRepository
import com.alpsfly.aeroglide.core.data.AppState
import com.alpsfly.aeroglide.core.domain.usecase.location.ServiceStarter
import timber.log.Timber
import javax.inject.Inject

class CalibrationUseCase @Inject constructor(
    private val appRepository: AppRepository,
    private val calibrationProcessor: CalibrationProcessor, // Inject the "worker buddy"
    private val serviceStarter: ServiceStarter // Inject the service manager
) {
    operator fun invoke() {
        if (appRepository.appState.value == AppState.Calibrating) {
            Timber.w("Calibration is already in progress, ignoring request.")
            return
        }
        Timber.i("CalibrationUseCase: Commanding START.")

        // 1. Tell the app to enter the 'Calibrating' state
        appRepository.enterCalibrationState()

        // 2. Tell the service to keep the hardware awake
        serviceStarter.startRecordingService()

        // 3. Tell the processor to start the calibration work
        calibrationProcessor.start {
            // This lambda block is the 'onCalibrationFinished' callback.
            // It is executed by the processor when its job is done.
            Timber.i("CalibrationUseCase: Received finish signal from processor.")

            // Now, the UseCase can decide whether to stop the hardware.
            // Check if a recording is in progress. If not, shut down the sensors.
            if (appRepository.appState.value != AppState.Recording) {
                Timber.i("No recording active, commanding service to stop.")
                serviceStarter.stopRecordingService()
            } else {
                Timber.i("Recording is active, leaving service running.")
            }
        }
    }

    // You could add a stop() method here if you need to manually cancel calibration
    fun stop() {
        if (appRepository.appState.value != AppState.Calibrating) return
        Timber.i("CalibrationUseCase: Commanding STOP.")
        calibrationProcessor.stop()
        // We don't stop the service here, as a recording might be active.
        // The AppState logic should handle service shutdown.
        appRepository.exitCalibrationState()
    }
}
