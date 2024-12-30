package com.alpsfly.aeroglide.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.domain.usecase.CalibrationUseCase
import com.alpsfly.aeroglide.core.presentation.CalibrationUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class FlightStatusViewModel @Inject constructor(
    sensorRepository: SensorRepository,
    doCalibration: CalibrationUseCase
) : ViewModel() {

    @OptIn(FlowPreview::class)
    val pressureFlow = sensorRepository.pressureDataSource.sample(1000.milliseconds)

    val locationFlow = sensorRepository.locationDataSource

    @OptIn(FlowPreview::class)
    val altitudeFlow = sensorRepository.altitudeFlow.sample(1000.milliseconds)

    @OptIn(FlowPreview::class)
    val climbrateFlow = sensorRepository.climbRateFlow.sample(1000.milliseconds)

    val calibrationUiState: StateFlow<CalibrationUiState> =
        doCalibration().map { result ->
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

}