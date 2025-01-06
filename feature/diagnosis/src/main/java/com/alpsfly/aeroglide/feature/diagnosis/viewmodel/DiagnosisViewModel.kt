package com.alpsfly.aeroglide.feature.diagnosis.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class DiagnosisViewModel @Inject constructor(
    sensorRepository: SensorRepository,
) : ViewModel() {
    val altitudeFlowRaw = sensorRepository.altitudeFlow
    val climbrateFlowRaw  = sensorRepository.climbRateFlow
    val altitudeFlowUi = sensorRepository.altitudeFlowUi
    val climbrateFlowUi = sensorRepository.climbrateFlowUi
    val pressureFlowUi = sensorRepository.pressureFlowUi
    val verticalAccelerationFlowUi = sensorRepository.verticalAccelerationFlowUi

    val altitudeCalibrationStatus = sensorRepository.altitudeCalibrationStatus
}