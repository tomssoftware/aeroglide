package com.alpsfly.aeroglide.feature.variometer.viewmodel

import androidx.lifecycle.ViewModel
import com.alpsfly.aeroglide.core.data.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.sample
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class VarioViewModel @Inject constructor(
    sensorRepository: SensorRepository,
) : ViewModel() {
    @OptIn(FlowPreview::class)
    val climbrate = sensorRepository.climbRateFlow.sample(1000.milliseconds)
}
