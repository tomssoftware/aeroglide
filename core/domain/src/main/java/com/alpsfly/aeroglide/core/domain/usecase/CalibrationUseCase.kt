package com.alpsfly.aeroglide.core.domain.usecase

import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.model.hardware.Calibration
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.timeout
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class CalibrationUseCase @Inject constructor(
    private val sensorRepository: SensorRepository
) {
    operator fun invoke(): Flow<Calibration> {
        sensorRepository.enableListener()
        return combine(sensorRepository.pressureFlowUi, sensorRepository.locationFlowUi) { p, l ->
            Timber.i("Calibration: ${l.verticalAccuracy}, ${l.horizontalAccuracy}, ${p.pressure}")
            val pressure = p.pressure * 100f
            val altitude = l.altitude
            val isCalibrated = (l.verticalAccuracy < 1.5 && l.horizontalAccuracy < 15 && pressure > 0f)
            Calibration(
                timestamp = System.currentTimeMillis(),
                isCalibrated = isCalibrated,
                altitude0 = altitude,
                pressure0 = pressure,
                verticalAccuracy = l.verticalAccuracy,
                horizontalAccuracy = l.horizontalAccuracy
            )
        }.takeWhile {
            it.isCalibrated.not()
        }.onCompletion {
            sensorRepository.disableListener()
            if (it == null) {
                emit(Calibration())
            }
        }
    }
}