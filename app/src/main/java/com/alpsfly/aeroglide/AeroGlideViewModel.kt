package com.alpsfly.aeroglide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.domain.usecase.FlightSessionCoordinatorUseCase
import com.alpsfly.aeroglide.core.domain.usecase.VarioToneUseCase
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
    private val flightCoordinator: FlightSessionCoordinatorUseCase,
    private val varioToneUseCase: VarioToneUseCase,
) : ViewModel() {

    fun toggleRecording() = flightCoordinator.toggleRecording()
    fun calibrate() = flightCoordinator.startCalibration()
    fun permissionRequest() = flightCoordinator.permissionRequest()
    fun permissionGranted() = flightCoordinator.permissionGranted()
    fun permissionDenied() = flightCoordinator.permissionDenied()

    // Forward the flow.
    // Using stateIn ensures it's a hot flow that replays the latest value
    // to new subscribers (the UI) immediately.
    val appState: StateFlow<AppState> = appStateManager.appState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppState.Idle, // Or appStateManager.value if available
        )

    // --- Vario tone ---

    /**
     * Reflects whether the variometer tone engine is currently active.
     * Observed by the UI to toggle between `volume_up_24px` and `volume_off_24px` icons.
     *
     * SSOT: state lives in [VarioToneUseCase]; this ViewModel only forwards it.
     */
    val isToneEnabled: StateFlow<Boolean> = varioToneUseCase.isToneEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    /**
     * Toggles the variometer tone engine on or off.
     * Delegates to [VarioToneUseCase.enable] / [VarioToneUseCase.disable].
     */
    fun toggleTone() {
        if (isToneEnabled.value) varioToneUseCase.disable()
        else varioToneUseCase.enable()
    }

    override fun onCleared() {
        super.onCleared()
        Timber.i("CLEARED VIEWMODEL")
    }
}
