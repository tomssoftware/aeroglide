package com.alpsfly.aeroglide.core.domain.usecase

import com.alpsfly.aeroglide.core.common.di.ApplicationScope
import com.alpsfly.aeroglide.core.domain.usecase.state.AppState
import com.alpsfly.aeroglide.core.domain.usecase.state.AppStateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlightSessionCoordinatorUseCase @Inject constructor(
    private val appStateManager: AppStateManager,
    private val calibrationUseCase: CalibrationUseCase,
    private val recordingUseCase: RecordingUseCase,
    private val autoStartUseCase: AutoStartUseCase,
    @param:ApplicationScope private val applicationScope: CoroutineScope
) {

    init {
        // The Coordinator's primary job is to listen to the app's state and react.
        listenToAppState()
    }

    private fun listenToAppState() {
        appStateManager.appState
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

                        // The coordinator now decides when to enable auto-start.
                        val shouldAutoStart = false // Get this from a SettingsProvider
                        if (shouldAutoStart) {
                            autoStartUseCase.enable()
                        }
                        Timber.d("Coordinator: App is Ready. Waiting for user action.")
                    }

                    AppState.Recording -> {
                        // When a recording starts (manually or auto), disable the detector.
                        // autoStartUseCase.disable()
                    }

                    else -> {
                        // For other states like Calibrating, Recording, etc., the coordinator
                        // doesn't need to initiate an action, it just observes.
                        Timber.d("Coordinator: No action needed for state $state.")
                    }
                }
            }.launchIn(applicationScope)
    }

    /**
     * Toggles the recording state.
     * This is the single entry point for the UI to start or stop a recording.
     */
    fun onToggleRecording() {
        val currentState = appStateManager.appState.value
        Timber.i("Coordinator: onToggleRecording called from state: $currentState")

        when (currentState) {
            AppState.Recording -> {
                // If we are currently recording, the command is to stop.
                recordingUseCase.stopRecording()
            }

            AppState.Ready, AppState.AutoStart -> {
                // If we are in a state where recording is allowed, the command is to start.
                // The coordinator is responsible for creating a unique ID for the new activity.
                recordingUseCase.startRecording(System.currentTimeMillis())
            }

            else -> {
                // If in any other state (like Idle or Calibrating), ignore the request.
                Timber.w("Coordinator: Ignoring toggle recording request from state $currentState.")
                return // Exit without changing the state machine
            }
        }

        // 3. After commanding the use case, tell the state machine to transition.
        // This decouples the action from the state change itself.
        appStateManager.onToggleRecording()
    }

    // We can delegate this call directly to the specialized use case.
    fun startCalibration() = calibrationUseCase()
}
