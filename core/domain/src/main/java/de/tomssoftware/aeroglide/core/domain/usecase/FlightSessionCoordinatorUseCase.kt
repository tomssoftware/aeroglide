package de.tomssoftware.aeroglide.core.domain.usecase

import de.tomssoftware.aeroglide.core.common.di.ApplicationScope
import de.tomssoftware.aeroglide.core.data.AutoStartSettingsProvider
import de.tomssoftware.aeroglide.core.data.SensorRepository
import de.tomssoftware.aeroglide.core.domain.usecase.state.AppSideEffect
import de.tomssoftware.aeroglide.core.domain.usecase.state.AppState
import de.tomssoftware.aeroglide.core.domain.usecase.state.AppStateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
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
    private val varioToneUseCase: VarioToneUseCase,
    private val sensorRepository: SensorRepository,
    private val autoStartSettingsProvider: AutoStartSettingsProvider,
    @param:ApplicationScope private val applicationScope: CoroutineScope
) {

    // --- Public State ---

    /**
     * The current [AppState] as a [StateFlow]. Forwarded from [AppStateManager] so that
     * the UI only needs a single entry point into the domain layer.
     */
    val appState: StateFlow<AppState> = appStateManager.appState

    /**
     * Whether the variometer tone engine is currently active.
     * Forwarded from [VarioToneUseCase] so that the UI only needs a single entry point.
     */
    val isToneEnabled: StateFlow<Boolean> = varioToneUseCase.isToneEnabled

    init {
        listenToSideEffects()
        listenToAutoStartSetting()
    }

    // --- State Observation ---

    /**
     * Reagiert auf [AppSideEffect]-Ereignisse aus dem [AppStateManager].
     *
     * Jede SideEffect kodiert exakt einen Übergang, dadurch wird kein prevState-Tracking
     * mehr benötigt. Die Logik ist flach und ohne verschachtelte when-Ausdrücke.
     */
    private fun listenToSideEffects() {
        appStateManager.sideEffects
            .onEach { effect ->
                Timber.i("Coordinator: SideEffect -> $effect")
                when (effect) {
                    is AppSideEffect.CalibrationStarted -> {
                        startCalibration()
                    }
                    is AppSideEffect.CalibrationCompleted -> {
                        applicationScope.launch {
                            // Apply AutoStart setting that was already stored before the app
                            // reached Ready state. The settings Flow emits its initial value
                            // during Calibrating, so the guard in listenToAutoStartSetting()
                            // suppresses it. We re-check here once we are actually Ready.
                            val isAutoStartEnabled = autoStartSettingsProvider.isAutoStartEnabled.first()
                            if (isAutoStartEnabled)
                                autoStartUseCase.enable()

                            val currentLocation = sensorRepository.locationFlowUi.first()
                            Timber.d("Coordinator: Triggering DEM download")
                            downloadElevationUseCase(
                                currentLocation.latitude.toDouble(),
                                currentLocation.longitude.toDouble()
                            ).collect { downloadState ->
                                Timber.d("Coordinator: DEM download state: $downloadState")
                            }
                        }
                    }
                    is AppSideEffect.RecordingStarted -> {
                        Timber.i("Coordinator: Recording started id=${effect.activityId}")
                        recordingUseCase.startRecording(effect.activityId)
                        // Sync the AutoStartDetector: if AutoStart is active the detector
                        // is in WaitForTakeOff. A manual recording start means "we are
                        // flying now", so advance the detector to Flying so that the
                        // automatic landing detection kicks in immediately.
                        // manualStart() is safe to call even when AutoStart is disabled
                        // (detector not running) – it simply has no effect.
                        val isAutoStartEnabled = autoStartSettingsProvider.isAutoStartEnabled.first()
                        if (isAutoStartEnabled) {
                            autoStartUseCase.manualStart()
                        }
                    }
                    is AppSideEffect.RecordingStopped -> {
                        recordingUseCase.stopRecording()
                        // Always clean up: reset the detector to WaitForTakeOff and
                        // cancel all sensor/settings jobs regardless of the setting.
                        autoStartUseCase.disable()
                        val isAutoStartEnabled = autoStartSettingsProvider.isAutoStartEnabled.first()
                        if (isAutoStartEnabled) {
                            // Re-enable immediately: restarts ForegroundService + sensor
                            // collection so the detector is back in WaitForTakeOff and
                            // ready to detect the next take-off.
                            autoStartUseCase.enable()
                        }
                    }
                }
            }.launchIn(applicationScope)
    }

    /**
     * Observes [AutoStartSettingsProvider.isAutoStartEnabled] directly and enables/disables
     * the AutoStart processor whenever the setting changes – only while in [AppState.Ready].
     *
     * Replaces the former Ready → Ready state-machine transition that carried the autostart
     * flag through the state, which added complexity without adding clarity.
     */
    private fun listenToAutoStartSetting() {
        autoStartSettingsProvider.isAutoStartEnabled
            .onEach { enabled ->
                if (appStateManager.appState.value is AppState.Ready) {
                    Timber.d("Coordinator: AutoStart setting changed to $enabled while Ready.")
                    if (enabled)
                        autoStartUseCase.enable()
                    else
                        autoStartUseCase.disable()
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
        Timber.i("Coordinator: toggleRecording called from state: $currentState")

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

    /**
     * Toggles the variometer tone engine on or off.
     * Delegates to [VarioToneUseCase.enable] / [VarioToneUseCase.disable].
     */
    fun toggleTone() {
        if (varioToneUseCase.isToneEnabled.value) varioToneUseCase.disable()
        else varioToneUseCase.enable()
    }
}

