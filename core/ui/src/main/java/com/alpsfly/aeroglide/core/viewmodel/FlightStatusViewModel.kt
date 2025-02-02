package com.alpsfly.aeroglide.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.AppRepository
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.domain.usecase.CalibrationUseCase
import com.alpsfly.aeroglide.core.presentation.CalibrationUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FlightStatusViewModel @Inject constructor(
    appRepository: AppRepository,
    dataRepository: DataRepository,
    sensorRepository: SensorRepository,
    calibrationUseCase: CalibrationUseCase,
) : ViewModel() {

    val pressureFlow = sensorRepository.pressureFlowUi

    //val locationFlow = sensorRepository.locationDataSource
    val altitudeFlow = sensorRepository.altitudeFlowUi

    //val climbrateFlow = sensorRepository.climbrateFlowUi
    val verticalAccelerationFlow = sensorRepository.verticalAccelerationFlowUi

    val calibrationUiState: StateFlow<CalibrationUiState> =
        calibrationUseCase().map { result ->
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

    val climbrateFlow = combine(sensorRepository.climbrateFlowUi, appRepository.isRecording) { climbrate, isRecording ->
        if (isRecording) climbrate else null
    }.filterNotNull(
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val locationFlow = combine(sensorRepository.locationFlowUi, appRepository.isRecording) { location, isRecording ->
        if (isRecording) location else null
    }.filterNotNull(
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val activityFlow = dataRepository.getActivity(appRepository.activityId.value)
}