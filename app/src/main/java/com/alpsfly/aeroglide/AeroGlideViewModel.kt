package com.alpsfly.aeroglide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.domain.usecase.FlightSessionCoordinatorUseCase
import com.alpsfly.aeroglide.core.domain.usecase.state.AppState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AeroGlideViewModel @Inject constructor(
    private val flightCoordinator: FlightSessionCoordinatorUseCase,
) : ViewModel() {

    fun toggleRecording() = flightCoordinator.toggleRecording()
    fun calibrate() = flightCoordinator.startCalibration()
    fun permissionRequest() = flightCoordinator.permissionRequest()
    fun permissionGranted() = flightCoordinator.permissionGranted()
    fun permissionDenied() = flightCoordinator.permissionDenied()
    fun toggleTone() = flightCoordinator.toggleTone()

    val appState: StateFlow<AppState> = flightCoordinator.appState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppState.Idle,
        )

    val isToneEnabled: StateFlow<Boolean> = flightCoordinator.isToneEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )


    override fun onCleared() {
        super.onCleared()
        Timber.i("CLEARED VIEWMODEL")
    }
}
