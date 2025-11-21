package com.alpsfly.aeroglide.core.domain.usecase

import com.alpsfly.aeroglide.core.domain.usecase.location.ServiceStarter
import com.alpsfly.aeroglide.core.domain.usecase.state.AppState
import com.alpsfly.aeroglide.core.domain.usecase.state.AppStateManager
import timber.log.Timber
import javax.inject.Inject

/**
 * The "Manager" for the auto-start detection process.
 * This is a lightweight, stateless orchestrator.
 */
class AutoStartUseCase @Inject constructor(
    private val appStateManager: AppStateManager,        // The state manager for making decisions
    private val autoStartProcessor: AutoStartProcessor,  // The "worker buddy"
    private val serviceStarter: ServiceStarter           // The "hardware manager"
) {
    init {
        // The UseCase provides the implementation for the processor's callbacks.
        autoStartProcessor.onTakeOffDetected = {
            // The worker reported a take-off. The manager decides what to do.
            if (appStateManager.appState.value is AppState.AutoStart) {
                Timber.i("AutoStartUseCase: Received take-off signal, commanding state toggle.")
                appStateManager.onToggleRecording(0L) // todo: set id
            }
        }
        autoStartProcessor.onLandingDetected = {
            // The worker reported a landing. The manager decides what to do.
            if (appStateManager.appState.value is AppState.Recording) {
                Timber.i("AutoStartUseCase: Received landing signal, commanding state toggle.")
                appStateManager.onToggleRecording(0L) // todo: set id
            }
        }
    }

    /**
     * Commands the auto-start system to begin monitoring for a take-off.
     */
    fun enable() {
        Timber.i("AutoStartUseCase: Commanding START.")
        // 1. Tell the hardware manager to keep the sensors awake.
        serviceStarter.startRecordingService()
        // 2. Tell the worker to start its detection logic.
        autoStartProcessor.start()
    }

    /**
     * Commands the auto-start system to stop monitoring.
     */
    fun disable() {
        Timber.i("AutoStartUseCase: Commanding STOP.")
        // 1. Tell the worker to stop its detection logic.
        autoStartProcessor.stop()
        // 2. Tell the hardware manager that this process no longer needs the sensors.
        //    The service will decide if it should actually shut down.
        serviceStarter.stopRecordingService()
    }
}
