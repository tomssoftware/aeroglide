package com.alpsfly.aeroglide.core.domain.usecase

import com.alpsfly.aeroglide.core.common.di.ApplicationScope
import com.alpsfly.aeroglide.core.data.AppRepository
import com.alpsfly.aeroglide.core.data.AppState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlightSessionCoordinatorUseCase @Inject constructor(
    private val appRepository: AppRepository,
    private val calibrationUseCase: CalibrationUseCase,
    // ✅ 1. INJECT THE RECORDINGUSECASE
    private val recordingUseCase: RecordingUseCase,
    @param:ApplicationScope private val applicationScope: CoroutineScope
) {

    init {
        // The Coordinator's primary job is to listen to the app's state and react.
        listenToAppState()
    }

    private fun listenToAppState() {
        appRepository.appState
            .onEach { state ->
                Timber.i("Coordinator observes AppState: $state")
                // This is where the cross-use-case logic lives.
                when (state) {
                    AppState.Idle -> {
                        // The app has started. The coordinator decides the first
                        // action is to begin calibration.
                        Timber.i("Coordinator: App is Idle, commanding calibration to start.")
                        calibrationUseCase() // Call the invoke() operator
                    }

                    AppState.Ready -> {
                        // Calibration is done. The coordinator can now decide
                        // whether to enable auto-start based on a setting.
                        // For example:
                        // val shouldAutoStart = settingsProvider.getAutoStartEnabled()
                        // if (shouldAutoStart) { autoStartUseCase.enable() }
                        Timber.d("Coordinator: App is Ready. Waiting for user action.")
                    }

                    else -> {
                        // For other states like Calibrating, Recording, etc., the coordinator
                        // doesn't need to initiate an action, it just observes.
                        Timber.d("Coordinator: No action needed for state $state.")
                    }
                }
            }
            .launchIn(applicationScope)
    }

    // ✅ 2. ADD THE PUBLIC FUNCTION FOR THE VIEWMODEL TO CALL
    /**
     * Toggles the recording state.
     * This is the single entry point for the UI to start or stop a recording.
     */
    fun onToggleRecording() {
        val currentState = appRepository.appState.value
        Timber.i("Coordinator: onToggleRecording called from state: $currentState")

        if (currentState is AppState.Recording) {
            // If we are currently recording, the command is to stop.
            recordingUseCase.stopRecording()
        } else if (currentState == AppState.Ready || currentState == AppState.AutoStart) {
            // If we are in a state where recording is allowed, the command is to start.
            // The coordinator is responsible for creating a unique ID for the new activity.
            appRepository.setActivityId(System.currentTimeMillis())
            recordingUseCase.startRecording(appRepository.activityId.value)
        } else {
            // If in any other state (like Idle or Calibrating), ignore the request.
            Timber.w("Coordinator: Ignoring toggle recording request from state $currentState.")
            return // Exit without changing the state machine
        }

        // 3. After commanding the use case, tell the state machine to transition.
        // This decouples the action from the state change itself.
        appRepository.onToggleRecording()
    }

    fun startCalibration() {
        // We can delegate this call directly to the specialized use case.
        calibrationUseCase()
    }
}
