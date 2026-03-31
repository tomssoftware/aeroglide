package com.alpsfly.aeroglide.core.domain.usecase

import com.alpsfly.aeroglide.core.domain.usecase.location.LocationServiceStarter
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
    private val locationServiceStarter: LocationServiceStarter
) {
    init {
        autoStartProcessor.onTakeOffDetected = {
            val state = appStateManager.appState.value
            if (state is AppState.Ready) {
                val newActivityId = System.currentTimeMillis()
                Timber.i("AutoStartUseCase: Take-off detected, starting recording with id=$newActivityId")
                appStateManager.toggleRecording(newActivityId)
            }
        }
        autoStartProcessor.onLandingDetected = {
            val state = appStateManager.appState.value
            if (state is AppState.Recording) {
                Timber.i("AutoStartUseCase: Landing detected, stopping recording.")
                appStateManager.toggleRecording(0L) // ID ignored when stopping
            }
        }
    }

    /**
     * Commands the auto-start system to begin monitoring for a take-off.
     */
    fun enable() {
        Timber.i("AutoStartUseCase: Commanding START.")
        locationServiceStarter.startForegroundService()
        autoStartProcessor.start()
    }

    /**
     * Commands the auto-start system to stop monitoring.
     */
    fun disable() {
        Timber.i("AutoStartUseCase: Commanding STOP.")
        autoStartProcessor.stop()
        locationServiceStarter.stopForegroundService()
    }

    /**
     * Manually starts a recording without waiting for the automatic take-off detection timer.
     *
     * Fires [AutoStartProcessor.onTakeOffDetected] immediately, which transitions
     * [AppState] to [AppState.Recording] via [AppStateManager.toggleRecording].
     * Only effective while the service is running (after [enable]).
     */
    fun manualStart() {
        Timber.i("AutoStartUseCase: Manual start.")
        autoStartProcessor.manualStart()
    }

    /**
     * Manually stops the current recording without waiting for the automatic landing detection timer.
     *
     * Fires [AutoStartProcessor.onLandingDetected] immediately, which transitions
     * [AppState] back to [AppState.Ready] via [AppStateManager.toggleRecording].
     * Only effective while recording is active.
     */
    fun manualStop() {
        Timber.i("AutoStartUseCase: Manual stop.")
        autoStartProcessor.manualStop()
    }
}
