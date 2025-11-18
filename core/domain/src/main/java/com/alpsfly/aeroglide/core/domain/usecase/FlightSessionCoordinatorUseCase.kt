package com.alpsfly.aeroglide.core.domain.usecase

import com.alpsfly.aeroglide.core.common.di.ApplicationScope
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.domain.usecase.state.AppState
import com.alpsfly.aeroglide.core.domain.usecase.state.AppStateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
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
    private val downloadDemUseCase: DownloadDemUseCase,
    private val sensorRepository: SensorRepository,
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
                    is AppState.Idle -> {
                        // The app has started. The coordinator decides the first
                        // action is to begin calibration.
                        Timber.i("Coordinator: App is Idle, commanding calibration to start.")
                        calibrationUseCase() // Call the invoke() operator
                    }

                    is AppState.Ready -> {
                        // When the app is ready, get the *first* available location.
                        val currentLocation = sensorRepository.locationFlowUi.first()
                        Timber.d("Coordinator: App is Ready. Triggering on-demand map check for location: $currentLocation")

                        // Use the location to call the use case.
                        downloadDemUseCase(currentLocation.latitude.toDouble(), currentLocation.longitude.toDouble())
                            .collect { downloadState ->
                                // The Coordinator can observe the result.
                                Timber.d("Coordinator: Dem download state: $downloadState")
                            }

                        // The coordinator now decides when to enable auto-start.
                        val shouldAutoStart = false // Get this from a SettingsProvider
                        if (shouldAutoStart) {
                            autoStartUseCase.enable()
                        }
                        Timber.d("Coordinator: App is Ready. Waiting for user action.")
                    }

                    is AppState.Recording -> {
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

        if (currentState is AppState.Recording) {
            // --- STOPPING A RECORDING ---
            recordingUseCase.stopRecording()
            // We do NOT need to provide an ID. The state machine will get it from the fromState.
            appStateManager.onToggleRecording(0L)

        } else if (currentState is AppState.Ready || currentState is AppState.AutoStart) {
            // --- STARTING A RECORDING ---
            // 1. The coordinator creates the unique ID for the new flight.
            val newActivityId = System.currentTimeMillis()

            // 3. It commands the RecordingUseCase to start its work.
            recordingUseCase.startRecording(newActivityId) // The UseCase no longer needs the ID passed to it.

            // 4. It tells the state machine to transition, PASSING IN THE NEW ID.
            appStateManager.onToggleRecording(newActivityId)

        } else {
            Timber.w("Coordinator: Ignoring toggle recording request from state $currentState.")
        }
    }

    // We can delegate this call directly to the specialized use case.
    fun startCalibration() = calibrationUseCase()
}
