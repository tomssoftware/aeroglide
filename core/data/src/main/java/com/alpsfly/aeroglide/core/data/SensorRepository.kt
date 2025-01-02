package com.alpsfly.aeroglide.core.data

import android.content.Context
import android.hardware.SensorEventCallback
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import com.alpsfly.aeroglide.core.common.Limits
import com.alpsfly.aeroglide.core.common.chunked
import com.alpsfly.aeroglide.core.data.util.IKalmanFilter
import com.alpsfly.aeroglide.core.data.util.KalmanFilter
import com.alpsfly.aeroglide.core.data.util.Q_ACCELERATION
import com.alpsfly.aeroglide.core.data.util.R_ALTITUDE
import com.alpsfly.aeroglide.core.data.util.getVerticalAcceleration
import com.alpsfly.aeroglide.core.hardware.SensorFrequency
import com.alpsfly.aeroglide.core.hardware.linearAccelerationSensorDataFlow
import com.alpsfly.aeroglide.core.hardware.locationDataFlow
import com.alpsfly.aeroglide.core.hardware.pressureSensorDataFlow
import com.alpsfly.aeroglide.core.hardware.rotationVectorSensorDataFlow
import com.alpsfly.aeroglide.core.model.hardware.Calibration
import com.alpsfly.aeroglide.core.model.hardware.SensorData
import com.alpsfly.aeroglide.core.model.hardware.SensorType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds

interface SensorRepository {

    /** Pressure sensor flow with 200ms delay */
    val pressureDataSource: Flow<SensorData>

    /** Location sensor flow with 1000ms delay */
    val locationDataSource: Flow<Location>

    val pressureFlowUi: Flow<SensorData>

    /** Fused sensor */
    val climbRateFlow: Flow<SensorData>
    val climbrateFlowUi: Flow<SensorData>

    /** Fused sensor */
    val altitudeFlow: Flow<SensorData>
    val altitudeFlowUi: Flow<SensorData>

    /** Status of altitude calibration **/
    val altitudeCalibrationStatus: StateFlow<Calibration>
}

@Singleton
class SensorRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sensorManager: SensorManager,
    private val locationManager: LocationManager
) : SensorRepository, SensorEventCallback() {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val kalmanFilter: IKalmanFilter =
        KalmanFilter(
            Q_ACCELERATION,
            R_ALTITUDE
        )

    /**
     * pressure state flow
     */
    override val pressureDataSource = sensorManager.pressureSensorDataFlow().shareSensorData()

    /**
     * Location state flow
     */
    override val locationDataSource = locationManager.locationDataFlow(context, 1000).shareSensorData()

    /**
     * Linear acceleration shared flow
     */
    private val linearAccelerationDataSource = sensorManager.linearAccelerationSensorDataFlow().shareSensorData()

    /**
     * Rotation vector shared flow
     */
    private val rotationVectorDataSource = sensorManager.rotationVectorSensorDataFlow().shareSensorData()

    /**
     * Vertical acceleration flow
     */
    private val verticalAccelerationFrequency = SensorFrequency()
    private val verticalAccelerationFlow: Flow<SensorData>
        get() {
            return combine(linearAccelerationDataSource, rotationVectorDataSource) { a, r ->
                getVerticalAcceleration(a, r)
            }.chunked(200.milliseconds)
                .map { verticalAcceleration ->
                    verticalAcceleration.average().toFloat()
                }.map { averageVerticalAcceleration ->
                    val sensorData = SensorData(
                        type = SensorType.VerticalAcceleration,
                        timestamp = System.currentTimeMillis(),
                        frequency = verticalAccelerationFrequency.inc(),
                        values = floatArrayOf(averageVerticalAcceleration)
                    )
                    //Timber.v(sensorData.toString())
                    sensorData
                }
        }

    private val pressureFlowFrequencyUi = SensorFrequency()
    override val pressureFlowUi: Flow<SensorData>
        get() {
            return pressureDataSource
                .map { d -> d.values[0] }
                .chunked(1000.milliseconds)
                .map { l ->
                    val sensorData =
                        SensorData(
                            type = SensorType.Pressure,
                            timestamp = System.currentTimeMillis(),
                            frequency = pressureFlowFrequencyUi.inc(),
                            values = floatArrayOf(l.average().toFloat())
                        )
                    // Timber.v(sensorData.toString())
                    sensorData
                }
        }

    /**
     * Altitude flow
     */
    private val _altitudeCalibrationStatus = MutableStateFlow(Calibration())
    override val altitudeCalibrationStatus: StateFlow<Calibration> = _altitudeCalibrationStatus.asStateFlow()
    private val altitudeFlowFrequency = SensorFrequency()
    private val altitudeFlowFrequencyUi = SensorFrequency()
    override val altitudeFlow: Flow<SensorData>
        get() {
            return combine(pressureDataSource, locationDataSource) { p, l ->
                val pressure = p.values[0] * 100f
                var altitude = l.altitude.toFloat()
                with(_altitudeCalibrationStatus.value) {
                    if (l.hasAccuracy() && l.hasVerticalAccuracy() && pressure != 0f && !isCalibrated) {
                        isCalibrated = true
                        pressure0 = pressure
                        altitude0 = altitude
                        verticalAccuracy = l.verticalAccuracyMeters
                        horizontalAccuracy = l.verticalAccuracyMeters
                    }
                    if (altitude0 != 0f && pressure0 != 0f && pressure != 0f) {
                        altitude = calcAltitude(pressure, pressure0, altitude0)
                    }
                }
                val sensorData = SensorData(
                    type = SensorType.Altitude,
                    timestamp = System.currentTimeMillis(),
                    values = floatArrayOf(altitude),
                    frequency = altitudeFlowFrequency.inc()
                )
                // Timber.v(sensorData.toString())
                sensorData
            }
        }

    override val altitudeFlowUi: Flow<SensorData>
        get() {
            return altitudeFlow
                .map { d -> d.values[0] }
                .chunked(1000.milliseconds)
                .map { l ->
                    val sensorData = SensorData(
                        type = SensorType.Altitude,
                        timestamp = System.currentTimeMillis(),
                        frequency = altitudeFlowFrequencyUi.inc(),
                        values = floatArrayOf(l.average().toFloat())
                    )
                    // Timber.v(sensorData.toString())
                    sensorData
                }
        }

    /**
     * Climbrate flow
     */
    private val climbrateFlowFrequency = SensorFrequency()
    private val climbrateFlowFrequencyUi = SensorFrequency()
    override val climbRateFlow: Flow<SensorData>
        get() {
            return altitudeFlow.combine(verticalAccelerationFlow) { altitudeData, accelerationData ->
                if (accelerationData.frequency > 0) {
                    kalmanFilter.predict(accelerationData.values[0], 1f / accelerationData.frequency)
                }
                if (altitudeData.frequency > 0) {
                    kalmanFilter.update(altitudeData.values[0])
                }
                val sensorData = SensorData(
                    type = SensorType.Climbrate,
                    timestamp = System.currentTimeMillis(),
                    values = floatArrayOf(kalmanFilter.climbrate),
                    frequency = climbrateFlowFrequency.inc()
                )
                // Timber.v(sensorData.toString())
                sensorData
            }
        }

    override val climbrateFlowUi: Flow<SensorData>
        get() {
            return climbRateFlow
                .map { s -> s.values[0] }
                .chunked(1000.milliseconds)
                .map { l ->
                    val sensorData = SensorData(
                        type = SensorType.Climbrate,
                        timestamp = System.currentTimeMillis(),
                        frequency = climbrateFlowFrequencyUi.inc(),
                        values = floatArrayOf(l.average().toFloat())
                    )
                    // Timber.v(sensorData.toString())
                    sensorData
                }
        }

    private fun calcAltitude(pressure: Float, pressure0: Float, altitude0: Float): Float {
        if (altitude0 in Limits.minAltitude..Limits.maxAltitude &&
            pressure0 in Limits.minPressure..Limits.maxPressure &&
            pressure in Limits.minPressure..Limits.maxPressure
        ) {
            val h0 = altitude0.toDouble() // meter
            val ph = pressure.toDouble() // pascal
            val p0 = pressure0.toDouble() // pascal

            /**
             * https://de.wikipedia.org/wiki/Barometrische_Höhenformel
             * Th = 288.15f (15°C)
             * h = (Th/0.0065) * (1.0 - (ph/p0)^(1/5.255))
             **/
            val e = 0.1902949571836346 /* 1 / 5.255 */
            val h = 44330.769 * (1.0 - (ph / p0).pow(e))

            return (h0 + h).toFloat()
        } else {
            return 0f
        }
    }

    private fun <T> Flow<T>.shareSensorData(stopTimeoutMillis: Long = 5000): Flow<T> = shareIn(
        scope = repositoryScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
        replay = 1
    )
}



