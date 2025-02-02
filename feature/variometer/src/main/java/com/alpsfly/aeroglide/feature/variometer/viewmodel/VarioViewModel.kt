package com.alpsfly.aeroglide.feature.variometer.viewmodel

import androidx.lifecycle.ViewModel
import com.alpsfly.aeroglide.core.data.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VarioViewModel @Inject constructor(
    sensorRepository: SensorRepository,
) : ViewModel() {
    val climbrate = sensorRepository.climbrateFlowUi
}
