package com.alpsfly.aeroglide.core.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.alpsfly.aeroglide.core.data.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.sample
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SensorViewModel @Inject constructor(
    sensorRepository: SensorRepository,
) : ViewModel() {
    @OptIn(FlowPreview::class)
    val acceleration = sensorRepository.accelerometerDataSource.sample(1000.milliseconds)

    @OptIn(FlowPreview::class)
    val pressure = sensorRepository.pressureDataSource.sample(1000.milliseconds)

    // location
    val location = sensorRepository.locationDataSource

    @OptIn(FlowPreview::class)
    val verticalAcceleration = sensorRepository.verticalAccelerationFlow.sample(1000.milliseconds)

    @OptIn(FlowPreview::class)
    val altitude = sensorRepository.altitudeFlow.sample(1000.milliseconds)

    @OptIn(FlowPreview::class)
    val climbrate = sensorRepository.climbRateFlow.sample(1000.milliseconds)
}
