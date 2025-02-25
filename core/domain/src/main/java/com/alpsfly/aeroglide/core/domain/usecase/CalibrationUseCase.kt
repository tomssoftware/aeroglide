package com.alpsfly.aeroglide.core.domain.usecase

import com.alpsfly.aeroglide.core.data.AppRepository
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.model.database.Location
import com.alpsfly.aeroglide.core.model.hardware.Calibration
import com.alpsfly.aeroglide.core.model.hardware.SensorType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.takeWhile
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class CalibrationUseCase @Inject constructor(
    private val appRepository: AppRepository,
    private val sensorRepository: SensorRepository
) {
    operator fun invoke(): Flow<Calibration> {
        if (appRepository.isCalibrationRunning.value) {
            Timber.w("Calibration already running")
            return sensorRepository.calibration
        }

        sensorRepository.enableSensorListener()
        appRepository.startCalibration()
        accuracyProcessor.reset()
        val startOfCalibration = System.currentTimeMillis()
        var calibration = Calibration(timestamp = startOfCalibration)
        return combine(sensorRepository.pressureFlowUi, sensorRepository.locationFlowUi) { p, l ->
            val pressure = p.pressure * 100f
            val altitude = l.altitude
            Timber.v("Process calibration: $pressure, $altitude")

            calibration = Calibration(
                timestamp = startOfCalibration,
                isCalibrated = false,
                sensorType = getSensorType(l.hasHorizontalAccuracy, pressure),
                latitude = l.latitude,
                longitude = l.longitude,
                altitude0 = altitude,
                pressure0 = pressure,
                hasHorizontalAccuracy = l.hasHorizontalAccuracy,
                horizontalAccuracy = l.horizontalAccuracy,
                hasVerticalAccuracy = l.hasVerticalAccuracy,
                verticalAccuracy = l.verticalAccuracy
            )
            calibration
        }.takeWhile {
            !isLocationAccuracySufficient(it)
        }.onCompletion {
            sensorRepository.disableSensorListener()
            sensorRepository.setCalibration(calibration)
            appRepository.stopCalibration()
            with(calibration) {
                timestamp = System.currentTimeMillis()
                isCalibrated = accuracyProcessor.isCalibrated
                altitude0 = accuracyProcessor.altitude0
                Timber.i("Calibration finished: $isCalibrated, $pressure0, $altitude0")
            }
            emit(calibration)
        }
    }

    private fun getSensorType(hasHorizontalAccuracy: Boolean, pressure: Float): SensorType {
        return if (pressure != 0f) SensorType.Pressure
        else if (hasHorizontalAccuracy) SensorType.Location
        else SensorType.Unknown
    }

    private fun isLocationAccuracySufficient(calibration: Calibration): Boolean = with (calibration) {
        if (hasVerticalAccuracy) {
            accuracyProcessor.isAccuracyProcessed(verticalAccuracy, altitude0)
        } else {
            hasHorizontalAccuracy && accuracyProcessor.isAccuracyProcessed(horizontalAccuracy, altitude0)
        }
    }
}

class AccuracyProcessor {
    private val highAccuracy = mutableListOf<Float>()
    private val midAccuracy = mutableListOf<Float>()
    private val lowAccuracy = mutableListOf<Float>()
    private val noAccuracy = mutableListOf<Float>()
    var altitude0 = 0f
    var isCalibrated = false

    fun isAccuracyProcessed(accuracy: Float, altitude: Float) : Boolean {
        when (accuracy) {
            in 0.0f .. 3.5f -> highAccuracy.add(altitude)
            in 3.5f .. 5.0f -> midAccuracy.add(altitude)
            in 5.0f .. 20.0f -> lowAccuracy.add(altitude)
            else -> noAccuracy.add(altitude)
        }

        return when {
            (highAccuracy.size >= 10) -> {
                altitude0 = highAccuracy.average().toFloat()
                isCalibrated = true
                true
            }
            (midAccuracy.size >= 20) -> {
                altitude0 = midAccuracy.average().toFloat()
                isCalibrated = true
                true
            }
            (lowAccuracy.size >= 30) -> {
                altitude0 = lowAccuracy.average().toFloat()
                isCalibrated = true
                true
            }
            (noAccuracy.size >= 40) -> {
                altitude0 = noAccuracy.average().toFloat()
                isCalibrated = true
                true
            }
            else -> {
                false
            }
        }
    }

    fun reset() {
        highAccuracy.clear()
        midAccuracy.clear()
        lowAccuracy.clear()
        noAccuracy.clear()
        altitude0 = 0f
        isCalibrated = false
    }
}
private val accuracyProcessor = AccuracyProcessor()
