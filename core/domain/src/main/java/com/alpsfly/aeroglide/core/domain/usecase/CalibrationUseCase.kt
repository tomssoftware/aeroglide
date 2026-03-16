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

            // 4. Tell the app to enter the 'Ready' state
            appStateManager.onCalibrationFinished()
        }
    }
}
