package com.alpsfly.aeroglide.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.AppRepository
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.model.database.Activity
import com.alpsfly.aeroglide.core.model.hardware.Calibration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FlightStatusViewModel @Inject constructor(
    appRepository: AppRepository,
    dataRepository: DataRepository,
    sensorRepository: SensorRepository,
) : ViewModel() {

    val locationFlow = sensorRepository.locationFlowUi
    val altitudeFlow = sensorRepository.altitudeFlowUi
    val climbrateFlow = sensorRepository.climbrateFlowUi
    val glideRatioFlow = sensorRepository.glideRatioFlowUi
    private val calibrationFlow = sensorRepository.calibration

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

//    val climbrateFlow = combine(sensorRepository.climbrateFlowUi, appRepository.isRecording) { climbrate, isRecording ->
//        if (isRecording) climbrate else null
//    }.filterNotNull(
//    ).stateIn(
//        scope = viewModelScope,
//        started = SharingStarted.WhileSubscribed(5000),
//        initialValue = null
//    )
//
//    val locationFlow = combine(sensorRepository.locationFlowUi, appRepository.isRecording) { location, isRecording ->
//        if (isRecording) location else null
//    }.filterNotNull(
//    ).stateIn(
//        scope = viewModelScope,
//        started = SharingStarted.WhileSubscribed(5000),
//        initialValue = null
//    )

    val activityFlow = dataRepository.activityFlow
        .filterNotNull()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = Activity()
        )
}

sealed interface  CalibrationUiState {
    data object Loading : CalibrationUiState
    data class Success(
        val calibration: Calibration,
    ) : CalibrationUiState
}

