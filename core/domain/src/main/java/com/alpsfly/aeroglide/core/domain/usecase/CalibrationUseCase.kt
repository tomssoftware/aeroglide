package com.alpsfly.aeroglide.core.domain.usecase

import android.location.Location
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.model.hardware.SensorData
import com.alpsfly.aeroglide.core.model.hardware.Calibration
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.timeout
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class CalibrationUseCase @Inject constructor(
    private val sensorRepository: SensorRepository
) {
    operator fun invoke(): Flow<Calibration> {
        var lastCalibration = Calibration()
        return combine(sensorRepository.pressureFlowUi, sensorRepository.locationFlowUi) { p, l ->
            val pressure = p.pressure * 100f
            val altitude = l.altitude
            val calibration = if (l.verticalAccuracy < 1 && l.horizontalAccuracy < 10 && pressure > 0f) {
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
            emit(lastCalibration)
        }
    }
}