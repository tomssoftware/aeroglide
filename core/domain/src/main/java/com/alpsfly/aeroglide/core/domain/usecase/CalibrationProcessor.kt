package com.alpsfly.aeroglide.core.domain.usecase

import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.model.database.Calibration
import com.alpsfly.aeroglide.core.model.hardware.SensorType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.takeWhile
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalibrationProcessor @Inject constructor(
    private val sensorRepository: SensorRepository,
    private val applicationScope: CoroutineScope // Inject the application-level scope
) {
    private val accuracyProcessor = AccuracyProcessor()
    private var calibrationJob: Job? = null

    fun start(onCalibrationFinished: () -> Unit) {
        if (calibrationJob?.isActive == true) {
            Timber.w("Calibration is already in progress.")
            return
        }

        Timber.d("CalibrationProcessor: Starting.")
        accuracyProcessor.reset()
        sensorRepository.resetCalibration()

        val startOfCalibration = System.currentTimeMillis()
        var calibration = Calibration(timestamp = startOfCalibration)

        // The calibration logic now runs in the durable applicationScope
        calibrationJob = combine(
            sensorRepository.pressureFlowUi,
            sensorRepository.locationFlowUi
        ) { p, l ->
            val pressure = p.pressure
            val altitude = l.altitude

            Timber.d("CalibrationProcessor running: $pressure, $altitude")
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
            // Keep collecting until accuracy is sufficient
            !isLocationAccuracySufficient(it)
        }.onCompletion {
            // This runs when takeWhile completes (i.e., calibration is finished)
            Timber.i("CalibrationProcessor: Flow finished.")
            // Set the final calibration result before saving
            sensorRepository.setCalibration(calibration)
            with(calibration) {
                timestamp = System.currentTimeMillis()
                isCalibrated = accuracyProcessor.isCalibrated
                altitude0 = accuracyProcessor.altitude0
                Timber.d("Calibration finished: $isCalibrated, $pressure0, $altitude0")
            }
            // Invoke the callback to notify the manager that the job is done.
            onCalibrationFinished()
            emit(calibration)
        }.launchIn(applicationScope) // Launch in the background-safe scope
    }

    fun stop() {
        if (calibrationJob?.isActive == true) {
            Timber.d("CalibrationProcessor: Stopping.")
            calibrationJob?.cancel()
            calibrationJob = null
        }
    }

    private fun getSensorType(hasHorizontalAccuracy: Boolean, pressure: Float): SensorType {
        return if (pressure != 0f) SensorType.Pressure
        else if (hasHorizontalAccuracy) SensorType.Location
        else SensorType.Unknown
    }

    private fun isLocationAccuracySufficient(calibration: Calibration): Boolean =
        with(calibration) {
            if (hasVerticalAccuracy) {
                accuracyProcessor.isAccuracyProcessed(verticalAccuracy, altitude0)
            } else {
                hasHorizontalAccuracy && accuracyProcessor.isAccuracyProcessed(
                    horizontalAccuracy,
                    altitude0
                )
            }
        }
}

class AccuracyProcessor {
    var pressure0: Float = 0f
    var altitude0: Float = 0f
    var isCalibrated = false

    private val calibrationValues = mutableListOf<Float>()

    fun reset() {
        pressure0 = 0f
        altitude0 = 0f
        isCalibrated = false
        calibrationValues.clear()
    }

    fun isAccuracyProcessed(accuracy: Float, altitude: Float): Boolean {
        if (accuracy < 10) {
            calibrationValues.add(altitude)
        }
        if (calibrationValues.size > 20) {
            altitude0 = calibrationValues.average().toFloat()
            isCalibrated = true
            return true
        }
        return false
    }

    fun getCalibrationResult(currentCalibration: Calibration): Calibration {
        return currentCalibration.copy(
            isCalibrated = this.isCalibrated,
            altitude0 = this.altitude0,
            pressure0 = this.pressure0,
            timestamp = System.currentTimeMillis()
        )
    }
}
