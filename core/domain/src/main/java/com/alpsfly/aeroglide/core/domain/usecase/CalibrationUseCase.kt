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
        val startOfCalibration = System.currentTimeMillis()
        var calibration = Calibration(timestamp = startOfCalibration)
        return combine(sensorRepository.pressureFlowUi, sensorRepository.locationFlowUi) { p, l ->
            val pressure = p.pressure * 100f
            val altitude = l.altitude
            val isCalibrated = (l.verticalAccuracy < 1.5 && l.horizontalAccuracy < 10 && pressure > 0f)
            Timber.i("Calibration: $isCalibrated, ${l.verticalAccuracy}, ${l.horizontalAccuracy}, ${p.pressure}")
            calibration = Calibration(
                timestamp = startOfCalibration,
                isCalibrated = isCalibrated,
                altitude0 = altitude,
                pressure0 = pressure,
                verticalAccuracy = l.verticalAccuracy,
                horizontalAccuracy = l.horizontalAccuracy
            )
            calibration
        }.takeWhile {
            doCalibration(it)
        }.onCompletion {
            sensorRepository.disableListener()
            emit(calibration)
        }
    }

    private fun doCalibration(calibration: Calibration): Boolean {
        val doContinue = (System.currentTimeMillis() - calibration.timestamp) < 30.seconds.inWholeMilliseconds
        val isAccurate = (calibration.verticalAccuracy < REQUIRED_VERTICAL_ACCURACY
                && calibration.horizontalAccuracy < REQUIRED_HORIZONTAL_ACCURACY
                && calibration.pressure0 != 0f)
        return doContinue && !isAccurate
    }

    companion object {
        const val REQUIRED_VERTICAL_ACCURACY = 1.5f
        const val REQUIRED_HORIZONTAL_ACCURACY = 7.5f
    }
}