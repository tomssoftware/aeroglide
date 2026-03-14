package com.alpsfly.aeroglide.core.domain.usecase

import com.alpsfly.aeroglide.core.common.di.ApplicationScope
import com.alpsfly.aeroglide.core.data.AutoStartSettingsProvider
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.domain.usecase.state.AppState
import com.alpsfly.aeroglide.core.domain.usecase.state.AppStateManager
import com.alpsfly.aeroglide.core.domain.usecase.state.FromState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
     * This is the core of the coordinator pattern: business decisions live here rather than
     * being scattered across individual use cases or ViewModels.
     * Launched in [applicationScope] – the subscription never drops due to a lifecycle event.
     */
    private fun listenToAppState() {
        appStateManager.appState
            .onEach { state ->
                Timber.i("Coordinator observes AppState: $state")
                when (state) {
                    is AppState.Idle -> {
                        Timber.d("Coordinator: App is Idle.")
                    }

                    is AppState.Calibrating -> {
                        Timber.d("Coordinator: App is Calibrating, commanding calibration to start.")
                        onStartCalibration()
                    }

                    is AppState.Ready -> {
                        // Split by fromState so each re-entry to Ready has the right side effects.
                        when (state.fromState) {
                            FromState.Calibrating -> {
                                // First time the app is ready: pre-fetch elevation data for the
                                // current position so offline terrain is available immediately.
                                val currentLocation = sensorRepository.locationFlowUi.first()
                                Timber.d("Coordinator: App is Ready. Triggering DEM download for $currentLocation")
                                downloadElevationUseCase(
                                    currentLocation.latitude.toDouble(),
                                    currentLocation.longitude.toDouble()
                                ).collect { downloadState ->
                                    Timber.d("Coordinator: DEM download state: $downloadState")
                                }
                                enableAutoStartIfConfigured()
                            }

                            FromState.Recording -> {
                                // Covers both manual stop and auto-landing. Calling stopRecording()
                                // here is safe because RecordingProcessor.stop() is idempotent –
                                // it exits early when no recording jobs are active.
                                recordingUseCase.stopRecording()
                                enableAutoStartIfConfigured()
                            }

                            else -> {
                                Timber.d("Coordinator: App is Ready from ${state.fromState}. Waiting for user action.")
                            }
                        }
                    }

                    is AppState.AutoStart -> {
                        Timber.d("Coordinator: AutoStart mode active.")
                    }

                    is AppState.Recording -> {
                        if (state.fromState == FromState.AutoStart) {
                            // Auto-start triggered this recording via AutoStartUseCase, which only
                            // transitions the state machine. The coordinator must also kick off the
                            // actual data collection. The processor intentionally keeps running
                            // so it can detect the landing and stop recording automatically.
                            Timber.i("Coordinator: Auto-start triggered recording id=${state.activityId}")
                            recordingUseCase.startRecording(state.activityId)
                        }
                    }
                }
            }.launchIn(applicationScope)
    }

    /**
     * Reads [AutoStartSettingsProvider.isAutoStartEnabled] and, if enabled, transitions the
     * state machine to [AppState.AutoStart] and activates the [AutoStartUseCase].
     *
     * Called after calibration completes and after each recording stops so the pilot is
     * always ready for the next flight without manual interaction.
     */
    private suspend fun enableAutoStartIfConfigured() {
        val shouldAutoStart = autoStartSettingsProvider.isAutoStartEnabled.first()
        Timber.d("Coordinator: shouldAutoStart=$shouldAutoStart")
        if (shouldAutoStart) {
            appStateManager.onAutoStartEnabled(true)
            autoStartUseCase.enable()
        }
    }

    // --- Public API ---

    /**
     * Toggles the active recording from the UI.
     *
     * - When recording: stops the current session and transitions to [AppState.Ready].
     * - When ready or in auto-start mode: generates a new unique activity ID, starts data
     *   collection, and transitions to [AppState.Recording].
     *
     * Calls from any other state are ignored with a warning log.
     */
    fun onToggleRecording() {
        val currentState = appStateManager.appState.value
        Timber.i("Coordinator: onToggleRecording called from state: $currentState")

        if (currentState is AppState.Recording) {
            // Also called reactively by listenToAppState() when the state reaches
            // Ready(fromState=Recording), so this second call is a safe no-op.
            recordingUseCase.stopRecording()
            appStateManager.onToggleRecording(0L) // ID is ignored when stopping

        } else if (currentState is AppState.Ready || currentState is AppState.AutoStart) {
            // Use wall-clock millis as the activity ID: unique, monotonic, and directly
            // usable as a creation timestamp without an extra database round-trip.
            val newActivityId = System.currentTimeMillis()
            recordingUseCase.startRecording(newActivityId)
            appStateManager.onToggleRecording(newActivityId)

        } else {
            Timber.w("Coordinator: Ignoring toggle recording request from state $currentState.")
        }
    }

    /**
     * Triggers the calibration sequence via [CalibrationUseCase].
     * Called automatically when the state machine enters [AppState.Calibrating].
     */
    fun onStartCalibration() {
        calibrationUseCase()
    }

    /**
     * Forwards a runtime permission request to [AppStateManager].
     * @see AppStateManager.onPermissionRequest
     */
    fun onPermissionRequest() {
        appStateManager.onPermissionRequest()
    }

    /**
     * Forwards a granted permission result to [AppStateManager], advancing the app flow.
     * @see AppStateManager.onPermissionGranted
     */
    fun onPermissionGranted() {
        appStateManager.onPermissionGranted()
    }

    /**
     * Forwards a denied permission result to [AppStateManager].
     * @see AppStateManager.onPermissionDenied
     */
    fun onPermissionDenied() {
        appStateManager.onPermissionDenied()
    }
}



