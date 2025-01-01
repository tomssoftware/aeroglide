package com.alpsfly.aeroglide.core.domain.usecase

import android.location.Location
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.model.hardware.SensorData
import com.alpsfly.aeroglide.core.model.hardware.Calibration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class CalibrationUseCase @Inject constructor(
    private val sensorRepository: SensorRepository
) {
    operator fun invoke() : Flow<Calibration> =
        sensorRepository.pressureDataSource.mapToCalibrationResult(sensorRepository.locationDataSource)
}

private fun Flow<SensorData>.mapToCalibrationResult(locationFlow: Flow<Location>): Flow<Calibration> =
    combine(locationFlow) { p, l ->
        val pressure = p.values[0] * 100f
        val altitude = l.altitude.toFloat()
        if (l.hasAccuracy() && l.hasVerticalAccuracy() && pressure != 0f) {
            Calibration(
                timestamp = System.currentTimeMillis(),
                isCalibrated = true,
                altitude0 =altitude,
                pressure0 = pressure,
                verticalAccuracy = l.verticalAccuracyMeters,
                horizontalAccuracy = l.accuracy
            )
        } else {
            Calibration()
        }
    }

