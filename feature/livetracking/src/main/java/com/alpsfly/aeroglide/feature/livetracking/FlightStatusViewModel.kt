package com.alpsfly.aeroglide.feature.livetracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.AppRepository
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.domain.usecase.state.AppState
import com.alpsfly.aeroglide.core.domain.usecase.state.AppStateManager
import com.alpsfly.aeroglide.core.model.database.Activity
import com.alpsfly.aeroglide.core.model.database.Altitude
import com.alpsfly.aeroglide.core.model.database.Climbrate
import com.alpsfly.aeroglide.core.model.database.GlideRatio
import com.alpsfly.aeroglide.core.model.database.Location
import com.alpsfly.aeroglide.core.model.hardware.Calibration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// This data class is the single source of truth for the UI.
data class FlightUiState(
    val location: Location = Location(),
    val altitude: Altitude = Altitude(),
    val climbrate: Climbrate = Climbrate(),
    val glideRatio: GlideRatio = GlideRatio(),
    val activity: Activity? = null,
    val calibrationState: CalibrationUiState = CalibrationUiState.Loading,
    val isRecording: Boolean = false
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class FlightStatusViewModel @Inject constructor(
    appStateManager: AppStateManager,
    appRepository: AppRepository,
    dataRepository: DataRepository,
    sensorRepository: SensorRepository,
) : ViewModel() {

    // This is now the ONLY public state the UI needs to care about.
    val uiState: StateFlow<FlightUiState>

    init {
        // A flow that emits the current activity ONLY when recording, otherwise null.
        val activityFlow: StateFlow<Activity?> = appStateManager.appState
            .flatMapLatest { state ->
                if (state is AppState.Recording) {
                    appRepository.activityId.flatMapLatest { activityId ->
                        dataRepository.getActivityFlow(activityId)
                    }
                } else {
                    flowOf(null) // Emit null when not recording.
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

        val calibrationUiStateFlow: StateFlow<CalibrationUiState> = sensorRepository.calibration
            .map { result ->
                if (!result.isCalibrated) CalibrationUiState.Loading
                else CalibrationUiState.Success(result)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = CalibrationUiState.Loading
            )

        // 1. First, combine the core sensor data flows into an intermediate object.
        val sensorDataFlow = combine(
            sensorRepository.locationFlowUi,
            sensorRepository.altitudeFlowUi,
            sensorRepository.climbrateFlowUi,
            sensorRepository.glideRatioFlowUi
        ) { location, altitude, climbrate, glideRatio ->
            // Create a temporary holder for this group of data
            Triple(location, altitude, climbrate) to glideRatio
        }

        // 2. Now, combine the result of the first combine with the remaining flows.
        uiState = combine(
            sensorDataFlow, // This is our first group
            activityFlow,
            calibrationUiStateFlow,
            appStateManager.appState
        ) { sensorData, activity, calibration, appState ->
            // Deconstruct the results for readability
            val (location, altitude, climbrate) = sensorData.first
            val glideRatio = sensorData.second

            // Construct the final, complete UiState object
            FlightUiState(
                location = location,
                altitude = altitude,
                climbrate = climbrate,
                glideRatio = glideRatio,
                activity = activity,
                calibrationState = calibration,
                isRecording = appState is AppState.Recording
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FlightUiState()
        )
    }
}

sealed interface CalibrationUiState {
    data object Loading : CalibrationUiState
    data class Success(val calibration: Calibration) : CalibrationUiState
}
