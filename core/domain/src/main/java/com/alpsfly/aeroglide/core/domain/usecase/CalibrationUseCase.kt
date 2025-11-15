package com.alpsfly.aeroglide.core.domain.usecase

import com.alpsfly.aeroglide.core.domain.usecase.location.ServiceStarter
import com.alpsfly.aeroglide.core.domain.usecase.state.AppState
import com.alpsfly.aeroglide.core.domain.usecase.state.AppStateManager
import timber.log.Timber
import javax.inject.Inject

class CalibrationUseCase @Inject constructor(
    private val appStateManager: AppStateManager,
    private val calibrationProcessor: CalibrationProcessor, // Inject the "worker buddy"
    private val serviceStarter: ServiceStarter // Inject the service manager
) {
    operator fun invoke() {
        if (appStateManager.appState.value is AppState.Calibrating) {
            Timber.w("Calibration is already in progress, ignoring request.")
            return
        }
        Timber.i("CalibrationUseCase: Commanding START.")

        // 1. Tell the app to enter the 'Calibrating' state
        appStateManager.onCalibrationStarted()

        // 2. Tell the service to keep the hardware awake
        serviceStarter.startRecordingService()

        // 3. Tell the processor to start the calibration work
        calibrationProcessor.start {
            // This lambda block is the 'onCalibrationFinished' callback.
            // It is executed by the processor when its job is done.
            Timber.i("CalibrationUseCase: Received finish signal from processor.")

            // Now, the UseCase can decide whether to stop the hardware.
            // Check if a recording is in progress. If not, shut down the sensors.
            if (appStateManager.appState.value is AppState.Recording) {
                Timber.i("Recording is active, leaving service running.")
            } else {
                Timber.i("No recording active, commanding service to stop.")
                appStateManager.onCalibrationFinished()
                serviceStarter.stopRecordingService()

            }
        }
    }

    // You could add a stop() method here if you need to manually cancel calibration
    fun stop() {
        if (appStateManager.appState.value is AppState.Calibrating) {
            Timber.i("CalibrationUseCase: Commanding STOP.")
            calibrationProcessor.stop()
            // We don't stop the service here, as a recording might be active.
            // The AppState logic should handle service shutdown.
            appStateManager.onCalibrationFinished()
        }
    }
}
