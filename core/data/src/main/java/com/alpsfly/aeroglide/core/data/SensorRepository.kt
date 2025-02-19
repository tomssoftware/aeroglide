package com.alpsfly.aeroglide.core.data

import android.content.Context
import android.hardware.SensorEventCallback
import android.hardware.SensorManager
import android.location.LocationManager
import androidx.annotation.RequiresPermission
import com.alpsfly.aeroglide.core.common.Limits
import com.alpsfly.aeroglide.core.common.TimeProvider
import com.alpsfly.aeroglide.core.common.chunked
import com.alpsfly.aeroglide.core.common.di.SystemTime
import com.alpsfly.aeroglide.core.common.movingAverage
import com.alpsfly.aeroglide.core.data.util.IKalmanFilter
import com.alpsfly.aeroglide.core.data.util.KalmanFilter
import com.alpsfly.aeroglide.core.data.util.Q_ACCELERATION
import com.alpsfly.aeroglide.core.data.util.R_ALTITUDE
import com.alpsfly.aeroglide.core.data.util.getVerticalAcceleration
import com.alpsfly.aeroglide.core.hardware.SensorFrequency
import com.alpsfly.aeroglide.core.hardware.geoidCorrectionFlow
import com.alpsfly.aeroglide.core.hardware.linearAccelerationSensorDataFlow
import com.alpsfly.aeroglide.core.hardware.locationDataFlow
import com.alpsfly.aeroglide.core.hardware.pressureSensorDataFlow
import com.alpsfly.aeroglide.core.hardware.rotationVectorSensorDataFlow
import com.alpsfly.aeroglide.core.model.database.Altitude
import com.alpsfly.aeroglide.core.model.database.Climbrate
import com.alpsfly.aeroglide.core.model.database.GlideRatio
import com.alpsfly.aeroglide.core.model.database.Location
import com.alpsfly.aeroglide.core.model.database.Pressure
import com.alpsfly.aeroglide.core.model.hardware.Calibration
import com.alpsfly.aeroglide.core.model.hardware.SensorData
import com.alpsfly.aeroglide.core.model.hardware.SensorType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.shareIn
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds
import android.location.Location as SensorLocation

interface SensorRepository {

    /** Pressure sensor flow with 200ms delay */
    val pressureDataSource: Flow<SensorData>
    val pressureFlowUi: Flow<Pressure>

    /** Location sensor flow with 1000ms delay */
    val locationDataSource: Flow<SensorLocation>
    val geoidCorrectionDataSource: Flow<Float>
    val locationFlow: Flow<Location>
    val locationFlowUi: Flow<Location>

    val verticalAccelerationFlow: Flow<SensorData>
    val verticalAccelerationFlowUi: Flow<SensorData>

    /** Fused sensor */
    val climbRateFlow: Flow<SensorData>
    val climbrateFlowUi: Flow<Climbrate>

    /** Fused sensor */
    val altitudeFlow: Flow<SensorData>
    val altitudeFlowUi: Flow<Altitude>

    /** Fused sensor */
    val glideRatioFlow: Flow<SensorData>
    val glideRatioFlowUi: Flow<GlideRatio>

    val calibration: StateFlow<Calibration>
    fun setCalibration(calibration: Calibration)

    fun enableListener()
    fun disableListener()
}

@Singleton
class SensorRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sensorManager: SensorManager,
    private val locationManager: LocationManager,
    @SystemTime private val timeProvider: TimeProvider
) : SensorRepository, SensorEventCallback() {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Location state flow
     */
    private val enableListener = MutableStateFlow(false)
    override val locationDataSource = locationManager.locationDataFlow(
        context = context,
        enableLocationUpdates = enableListener,
        interval = 1000
    ).shareSensorData()

    override val geoidCorrectionDataSource = locationManager.geoidCorrectionFlow(
        context = context,
        enable = enableListener,
        interval = 1000
    ).shareSensorData()

    override val locationFlow: Flow<Location>
        get() {
            return combine(locationDataSource, geoidCorrectionDataSource) { l, geoidCorrection ->
                Location(
                    timestamp = System.currentTimeMillis(),
                    latitude = l.latitude.toFloat(),
                    longitude = l.longitude.toFloat(),
                    altitude = l.altitude.toFloat() - geoidCorrection,
                    bearing = l.bearing,
                    speed = l.speed,
                    horizontalAccuracy = l.accuracy,
                    verticalAccuracy = l.verticalAccuracyMeters,
                    bearingAccuracy = l.bearingAccuracyDegrees,
                    speedAccuracy = l.speedAccuracyMetersPerSecond,
                    provider = l.provider ?: "unknown"
                )
            }
        }

    override fun enableListener() {
        Timber.i("ENABLE SENSOR LISTENER")
        enableListener.value = true
    }

    override fun disableListener() {
        Timber.i("DISABLE SENSOR LISTENER")
        enableListener.value = false
    }

    /**
     * pressure state flow
     */
    override val pressureDataSource = sensorManager.pressureSensorDataFlow(
        enable = enableListener
    ) //.shareSensorData()

    /**
     * Linear acceleration shared flow
     */
    private val linearAccelerationDataSource = sensorManager.linearAccelerationSensorDataFlow(
        enable = enableListener
    ) //.shareSensorData()

    /**
     * Rotation vector shared flow
     */
    private val rotationVectorDataSource = sensorManager.rotationVectorSensorDataFlow(
        enable = enableListener
    ) //.shareSensorData()

    /**
     * Vertical acceleration flow
     */
    override val verticalAccelerationFlow: Flow<SensorData>
        get() {
            val sensorFrequency = SensorFrequency()
            return combine(linearAccelerationDataSource, rotationVectorDataSource) { a, r ->
                getVerticalAcceleration(a, r)
            }.map {
                SensorData(
                    type = SensorType.VerticalAcceleration,
                    timestamp = timeProvider.currentTimeMillis(),
                    frequency = sensorFrequency.inc(),
                    values = floatArrayOf(it)
                )
            }
        }

    override val verticalAccelerationFlowUi: Flow<SensorData>
        get() {
            val sensorFrequency = SensorFrequency()
            return verticalAccelerationFlow
                .map { d -> d.values[0] }
                .chunked(1000.milliseconds)
                .map { l ->
                    SensorData(
                        type = SensorType.VerticalAcceleration,
                        timestamp = System.currentTimeMillis(),
                        frequency = sensorFrequency.inc(),
                        values = floatArrayOf(l.average().toFloat())
                    )
                }
        }

    override val pressureFlowUi: Flow<Pressure>
        get() {
            val sensorFrequency = SensorFrequency()
            return pressureDataSource
                .map { d -> d.values[0] }
                .chunked(1000.milliseconds)
                .map { l ->
                    Pressure(
                        timestamp = System.currentTimeMillis(),
                        frequency = sensorFrequency.inc(),
                        pressure = l.average().toFloat()
                    )
                }
        }

    /**
     * Altitude flow
     */
    override val altitudeFlow: Flow<SensorData>
        get() {
            val sensorFrequency = SensorFrequency()
            return combine(pressureDataSource, locationFlow) { p, l ->
                val pressure = p.values[0] * 100f
                var altitude = l.altitude.toFloat()
                if (calibration.value.isCalibrated) {
                    altitude = calcAltitude(pressure, calibration.value.pressure0, calibration.value.altitude0)
                }
                SensorData(
                    type = SensorType.Altitude,
                    timestamp = System.currentTimeMillis(),
                    frequency = sensorFrequency.inc(),
                    values = floatArrayOf(altitude)
                )
            }
        }

    override val altitudeFlowUi: Flow<Altitude>
        get() {
            val sensorFrequency = SensorFrequency()
            return altitudeFlow
                .map { d -> d.values[0] }
                .chunked(1000.milliseconds)
                .map { l ->
                    Altitude(
                        timestamp = System.currentTimeMillis(),
                        frequency = sensorFrequency.inc(),
                        altitude = l.average().toFloat()
                    )
                }
        }

    /**
     * Climbrate flow
     */
    override val climbRateFlow: Flow<SensorData>
        get() {
            val sensorFrequency = SensorFrequency()
            val kalmanFilter: IKalmanFilter = KalmanFilter(Q_ACCELERATION, R_ALTITUDE)
            var lastAltitude = 0L
            var lastVerticalAcceleration = 0L
            var isKalmanFilterConfigured = false
            return combine(altitudeFlow, verticalAccelerationFlow) { a, v ->
                if (a.timestamp != lastAltitude) {
                    if (lastAltitude == 0L) {
                        kalmanFilter.configure(Q_ACCELERATION, R_ALTITUDE, a.values[0])
                        isKalmanFilterConfigured = true
                    }
                    kalmanFilter.update(a.values[0])
                    lastAltitude = a.timestamp
                }
                if (v.timestamp != lastVerticalAcceleration) {
                    if (isKalmanFilterConfigured && sensorFrequency.get() > 0) {
                        kalmanFilter.predict(v.values[0], 1f / sensorFrequency.get())
                    }
                    lastVerticalAcceleration = v.timestamp
                    sensorFrequency.inc()
                }
                kalmanFilter.climbrate
            }.movingAverage(20)
                .map { a ->
                    SensorData(
                        type = SensorType.Climbrate,
                        timestamp = System.currentTimeMillis(),
                        frequency = sensorFrequency.get(),
                        values = floatArrayOf(a)
                    )
                }
        }

    override val climbrateFlowUi: Flow<Climbrate>
        get() {
            val sensorFrequency = SensorFrequency()
            return climbRateFlow
                .map { s -> s.values[0] }
                .chunked(1000.milliseconds)
                .map { l ->
                    Climbrate(
                        timestamp = System.currentTimeMillis(),
                        frequency = sensorFrequency.inc(),
                        climbrate = l.average().toFloat()
                    )
                }
        }

    @OptIn(FlowPreview::class)
    override val locationFlowUi: Flow<Location>
        get() {
            return locationFlow.sample(1000.milliseconds)
        }

    override val glideRatioFlow: Flow<SensorData>
        get() {
            val sensorFrequency = SensorFrequency()
            return combine(locationDataSource, climbRateFlow) { l, c ->
                val glideRatio = when (c.values[0] <= -0.3) {
                    true -> l.speed / c.values[0]
                    false -> 0f
                }
                SensorData(
                    type = SensorType.GlideRatio,
                    timestamp = System.currentTimeMillis(),
                    frequency = sensorFrequency.inc(),
                    values = floatArrayOf(glideRatio)
                )
            }
        }

    override val glideRatioFlowUi: Flow<GlideRatio>
        get() {
            val sensorFrequency = SensorFrequency()
            return glideRatioFlow
                .map { s -> s.values[0] }
                .chunked(1000.milliseconds)
                .map { l ->
                    GlideRatio(
                        timestamp = System.currentTimeMillis(),
                        frequency = sensorFrequency.inc(),
                        glideRatio = l.average().toFloat()
                    )
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

    private val _calibration = MutableStateFlow(Calibration())
    override val calibration = _calibration.asStateFlow()
    override fun setCalibration(calibration: Calibration) {
        _calibration.value = calibration
    }

    private fun <T> Flow<T>.shareSensorData(stopTimeoutMillis: Long = 5000): Flow<T> = shareIn(
        scope = repositoryScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
        replay = 1
    )
}



