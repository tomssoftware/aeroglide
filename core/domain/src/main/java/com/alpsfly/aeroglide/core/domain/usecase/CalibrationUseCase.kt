package com.alpsfly.aeroglide.core.domain.usecase

import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.model.hardware.Calibration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.takeWhile
import timber.log.Timber
import javax.inject.Inject

class CalibrationUseCase @Inject constructor(
    private val sensorRepository: SensorRepository
) {
    operator fun invoke(): Flow<Calibration> {
        var lastCalibration = Calibration()
        sensorRepository.enableListener()
        return combine(sensorRepository.pressureFlowUi, sensorRepository.locationFlowUi) { p, l ->
            Timber.i("Calibration: ${l.verticalAccuracy}, ${l.horizontalAccuracy}, ${p.pressure}")
            val pressure = p.pressure * 100f
            val altitude = l.altitude
            val calibration = if (l.verticalAccuracy < 1.5 && l.horizontalAccuracy < 10 && pressure > 0f) {
                Calibration(
                    timestamp = System.currentTimeMillis(),
                    isCalibrated = true,
                    altitude0 = altitude,
                    pressure0 = pressure,
                    verticalAccuracy = l.verticalAccuracy,
                    horizontalAccuracy = l.horizontalAccuracy
                )
            } else {
                Calibration()
            }
            lastCalibration = calibration
            calibration
        }.takeWhile {
            !it.isCalibrated
        }.onCompletion {
            sensorRepository.disableListener()
            emit(lastCalibration)
        }
    }
}