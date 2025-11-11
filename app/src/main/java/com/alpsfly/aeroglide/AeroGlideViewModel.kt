package com.alpsfly.aeroglide

import androidx.lifecycle.ViewModel
import com.alpsfly.aeroglide.core.domain.usecase.FlightSessionCoordinatorUseCase
import com.alpsfly.aeroglide.core.domain.usecase.state.AppStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AeroGlideViewModel @Inject constructor(
    private val appStateManager: AppStateManager,
    private val flightCoordinator: FlightSessionCoordinatorUseCase
) : ViewModel() {

    val appState = appStateManager.appState

    fun onToggleRecording() = flightCoordinator.onToggleRecording()
    fun onReCalibrate() = flightCoordinator.startCalibration()

    fun startCalibration() {
        // The ViewModel just tells the coordinator its intent.
        flightCoordinator.startCalibration()
    }

    fun doEnableAutoStart(enable: Boolean) {
        // This is a direct state change, so calling AppRepository is fine.
        appStateManager.onAutoStartEnabled(enable)
    }

    override fun onCleared() {
        super.onCleared()
        Timber.i("CLEARED VIEWMODEL")
    }
}