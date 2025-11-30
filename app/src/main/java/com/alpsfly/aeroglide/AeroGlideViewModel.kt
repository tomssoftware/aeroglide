package com.alpsfly.aeroglide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.domain.usecase.FlightSessionCoordinatorUseCase
import com.alpsfly.aeroglide.core.domain.usecase.state.AppState
import com.alpsfly.aeroglide.core.domain.usecase.state.AppStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AeroGlideViewModel @Inject constructor(
    appStateManager: AppStateManager,
    private val flightCoordinator: FlightSessionCoordinatorUseCase
) : ViewModel() {

    fun onToggleRecording() = flightCoordinator.onToggleRecording()
    fun onReCalibrate() = flightCoordinator.onStartCalibration()
    fun onPermissionRequest() = flightCoordinator.onPermissionRequest()
    fun onPermissionGranted() = flightCoordinator.onPermissionGranted()
    fun onPermissionDenied() = flightCoordinator.onPermissionDenied()

    // Forward the flow.
    // Using stateIn ensures it's a hot flow that replays the latest value
    // to new subscribers (the UI) immediately.
    val appState: StateFlow<AppState> = appStateManager.appState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppState.Idle() // Or appStateManager.value if available
        )

    override fun onCleared() {
        super.onCleared()
        Timber.i("CLEARED VIEWMODEL")
    }
}