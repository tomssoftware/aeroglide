package com.alpsfly.aeroglide.core.domain.usecase

import com.alpsfly.aeroglide.core.domain.usecase.location.ServiceStarter
import com.alpsfly.aeroglide.core.domain.usecase.state.AppState
import com.alpsfly.aeroglide.core.domain.usecase.state.AppStateManager
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The "Manager" for the auto-start detection process.
 * Singleton to ensure callbacks are registered only once on the shared [AutoStartProcessor].
 */
@Singleton
class AutoStartUseCase @Inject constructor(
    private val appStateManager: AppStateManager,
    private val autoStartProcessor: AutoStartProcessor,
    private val serviceStarter: ServiceStarter
) {
    init {
        autoStartProcessor.onTakeOffDetected = {
            if (appStateManager.appState.value is AppState.AutoStart) {
                val newActivityId = System.currentTimeMillis()
                Timber.i("AutoStartUseCase: Take-off detected, starting recording with id=$newActivityId")
                appStateManager.onToggleRecording(newActivityId)
            }
        }
        autoStartProcessor.onLandingDetected = {
            if (appStateManager.appState.value is AppState.Recording) {
                Timber.i("AutoStartUseCase: Landing detected, stopping recording.")
                appStateManager.onToggleRecording(0L) // ID ignored when stopping
            }
        }
    }

    /**
     * Commands the auto-start system to begin monitoring for a take-off.
     */
    fun enable() {
        Timber.i("AutoStartUseCase: Commanding START.")
        serviceStarter.startRecordingService()
        autoStartProcessor.start()
    }

    /**
     * Commands the auto-start system to stop monitoring.
     */
    fun disable() {
        Timber.i("AutoStartUseCase: Commanding STOP.")
        autoStartProcessor.stop()
        serviceStarter.stopRecordingService()
    }
}
