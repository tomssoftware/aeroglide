package com.alpsfly.aeroglide.feature.livetracking

import androidx.lifecycle.ViewModel
import com.alpsfly.aeroglide.core.data.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VariometerViewModel @Inject constructor(
    sensorRepository: SensorRepository,
) : ViewModel() {
    val climbrate = sensorRepository.climbrateFlowUi
}
