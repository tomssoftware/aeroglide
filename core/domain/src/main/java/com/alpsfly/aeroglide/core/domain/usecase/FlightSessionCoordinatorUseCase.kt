package com.alpsfly.aeroglide.core.domain.usecase

import com.alpsfly.aeroglide.core.common.di.ApplicationScope
import com.alpsfly.aeroglide.core.data.AutoStartSettingsProvider
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.domain.usecase.state.AppState
import com.alpsfly.aeroglide.core.domain.usecase.state.AppStateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central coordinator that reacts to [AppState] transitions and orchestrates all use cases.
 *
 * This is the single authority for cross-use-case decisions such as when to start/stop
 * a recording, trigger calibration, or enable auto-start detection. The UI and other
 * components interact with the app only through this coordinator.
 *
 * All state observation runs in [applicationScope] so it survives configuration changes.
 *
 * @see AppStateManager
 * @see AutoStartUseCase
 * @see RecordingUseCase
 */
// @Singleton: exactly one coordinator must exist to prevent duplicate state observers
// and to guarantee that the init block launches the state listener only once.
@Singleton
class FlightSessionCoordinatorUseCase @Inject constructor(
    private val appStateManager: AppStateManager,
    private val calibrationUseCase: CalibrationUseCase,
    private val recordingUseCase: RecordingUseCase,
    private val autoStartUseCase: AutoStartUseCase,
    private val downloadElevationUseCase: DownloadElevationUseCase,
    private val sensorRepository: SensorRepository,
    private val autoStartSettingsProvider: AutoStartSettingsProvider,
    @param:ApplicationScope private val applicationScope: CoroutineScope
) {

    init {
        listenToAppState()
    }

    // --- State Observation ---

    /**
     * Subscribes to [AppStateManager.appState] and dispatches work to the appropriate use cases.
     *
     * Tracks the previous state via a local `prevState` variable so each transition can be
     * handled based on both old and new state – without any runtime casts or embedded history
     * in [AppState].
     * Launched in [applicationScope] – the subscription never drops due to a lifecycle event.
     */
    private fun listenToAppState() {
        var prevState: AppState = appStateManager.appState.value

        appStateManager.appState
            .onEach { current ->
                val prev = prevState
                prevState = current
                Timber.i("Coordinator: $prev -> $current")

                when (current) {
                    is AppState.Idle -> Timber.d("Coordinator: App is Idle.")

                    is AppState.Calibrating -> {
                        Timber.d("Coordinator: App is Calibrating, commanding calibration to start.")
                        startCalibration()
                    }

                    is AppState.Ready -> when (prev) {
                        is AppState.Calibrating -> {
                            // Pre-fetch elevation data for the current position so offline
                            // terrain is available immediately.
                            applicationScope.launch {
                                val currentLocation = sensorRepository.locationFlowUi.first()
                                Timber.d("Coordinator: Triggering DEM download for $currentLocation")
                                downloadElevationUseCase(
                                    currentLocation.latitude.toDouble(),
                                    currentLocation.longitude.toDouble()
                                ).collect { downloadState ->
                                    Timber.d("Coordinator: DEM download state: $downloadState")
                                }
                            }
                            val isAutoStartEnabled = autoStartSettingsProvider.isAutoStartEnabled.first()
                            if (isAutoStartEnabled) autoStartUseCase.enable()
                            else autoStartUseCase.disable()
                        }

                        is AppState.Recording -> {
                            // Covers both manual stop and auto-landing. stopRecording() is
                            // idempotent – it exits early when no recording jobs are active.
                            recordingUseCase.stopRecording()
                        }

                        is AppState.Ready -> {
                            // Ready → Ready: autostart was toggled in Settings
                            if (current.autostart)
                                autoStartUseCase.enable()
                            else
                                autoStartUseCase.disable()
                        }

                        else -> Timber.d("Coordinator: App is Ready from $prev. Waiting for user action.")
                    }

                    is AppState.Recording -> {
                        if (prev is AppState.Ready) {
                            // Handles BOTH manual start and autostart uniformly.
                            // startRecording() is always triggered here, never duplicated.
                            Timber.i("Coordinator: Recording started id=${current.activityId}")
                            recordingUseCase.startRecording(current.activityId)
                        }
                    }
                }
            }.launchIn(applicationScope)
    }


    // --- Public API ---

    /**
     * Toggles the active recording from the UI.
     *
     * - **Recording → stop:** transitions to [AppState.Ready]; the reactive observer calls
     *   [RecordingUseCase.stopRecording] automatically.
     * - **Ready → start:** generates a unique activity ID and transitions to [AppState.Recording];
     *   the reactive observer calls [RecordingUseCase.startRecording] automatically.
     * - All other states are ignored with a warning log.
     */
    fun toggleRecording() {
        val currentState = appStateManager.appState.value
        Timber.i("Coordinator: onToggleRecording called from state: $currentState")

        when (currentState) {
            is AppState.Recording -> {
                // ID is unused when stopping; the observer handles stopRecording().
                appStateManager.toggleRecording(0L)
            }

            is AppState.Ready -> {
                // Wall-clock millis as activity ID: unique, monotonic, usable as creation timestamp.
                val newActivityId = System.currentTimeMillis()
                appStateManager.toggleRecording(newActivityId)
            }

            else -> Timber.w("Coordinator: Ignoring toggle recording request from state $currentState.")
        }
    }

    /**
     * Triggers the calibration sequence via [CalibrationUseCase].
     * Called automatically when the state machine enters [AppState.Calibrating].
     */
    fun startCalibration() {
        calibrationUseCase()
    }

    /**
     * Forwards a runtime permission request to [AppStateManager].
     * @see AppStateManager.permissionRequest
     */
    fun permissionRequest() {
        appStateManager.permissionRequest()
    }

    /**
     * Forwards a granted permission result to [AppStateManager], advancing the app flow.
     * @see AppStateManager.permissionGranted
     */
    fun permissionGranted() {
        appStateManager.permissionGranted()
    }

    /**
     * Forwards a denied permission result to [AppStateManager].
     * @see AppStateManager.permissionDenied
     */
    fun permissionDenied() {
        appStateManager.permissionDenied()
    }
}

