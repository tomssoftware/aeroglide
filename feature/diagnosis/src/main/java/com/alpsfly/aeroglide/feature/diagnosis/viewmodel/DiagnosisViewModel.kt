package com.alpsfly.aeroglide.feature.diagnosis.viewmodel

import androidx.lifecycle.ViewModel
import com.alpsfly.aeroglide.core.data.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

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

    val calibrationFlow = sensorRepository.calibration
}