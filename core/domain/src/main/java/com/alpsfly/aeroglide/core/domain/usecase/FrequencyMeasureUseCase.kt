package com.alpsfly.aeroglide.core.domain.usecase

import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.model.hardware.SensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FrequencyMeasureUseCase @Inject constructor(
    private val sensorRepository: SensorRepository
) {
    operator fun invoke(): Flow<Boolean> =
        sensorRepository.climbRateFlow.mapToFrequencyMeasureResult()
}

private fun Flow<SensorData>.mapToFrequencyMeasureResult(): Flow<Boolean> {
    return map { s -> (s.frequency > 5f) }
}


