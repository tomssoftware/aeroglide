package com.alpsfly.aeroglide.feature.livetracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.AppRepository
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.model.database.Activity
import com.alpsfly.aeroglide.core.model.hardware.Calibration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FlightStatusViewModel @Inject constructor(
    appRepository: AppRepository,
    private val dataRepository: DataRepository,
    sensorRepository: SensorRepository,
) : ViewModel() {

    val locationFlow = sensorRepository.locationFlowUi
    val altitudeFlow = sensorRepository.altitudeFlowUi
    val climbrateFlow = sensorRepository.climbrateFlowUi
    val glideRatioFlow = sensorRepository.glideRatioFlowUi
    private val calibrationFlow = sensorRepository.calibration

    //private val activityId = appRepository.activityId
    private val isRecording = appRepository.isRecording

    val calibrationUiState: StateFlow<CalibrationUiState> =
        calibrationFlow
            .map { result ->
                if (!result.isCalibrated) {
                    CalibrationUiState.Loading
                } else {
                    CalibrationUiState.Success(result)
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = CalibrationUiState.Loading
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activityFlow: StateFlow<Activity> = isRecording
        .flatMapLatest { isRecording ->
            if (isRecording) {
                appRepository.activityId.flatMapLatest { activityId ->
                    dataRepository.getActivityFlow(activityId)
                }
            } else {
                flowOf() // Emit an empty flow if the boolean state is false
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily, // todo: check if this is the best option
            initialValue = Activity()
        )
}

sealed interface CalibrationUiState {
    data object Loading : CalibrationUiState
    data class Success(
        val calibration: Calibration,
    ) : CalibrationUiState
}

