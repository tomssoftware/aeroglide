package com.alpsfly.aeroglide.core.data

import android.content.Context
import android.hardware.SensorEventCallback
import android.hardware.SensorManager
import android.location.LocationManager
import com.alpsfly.aeroglide.core.common.Limits
import com.alpsfly.aeroglide.core.common.TimeProvider
import com.alpsfly.aeroglide.core.common.chunked
import com.alpsfly.aeroglide.core.common.di.ApplicationScope
import com.alpsfly.aeroglide.core.common.di.SystemTime
import com.alpsfly.aeroglide.core.common.logDeviation
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
import com.alpsfly.aeroglide.core.model.hardware.Altitude
import com.alpsfly.aeroglide.core.model.hardware.Climbrate
import com.alpsfly.aeroglide.core.model.hardware.GlideRatio
import com.alpsfly.aeroglide.core.model.hardware.Location
import com.alpsfly.aeroglide.core.model.hardware.Pressure
import com.alpsfly.aeroglide.core.model.database.SyncState
import com.alpsfly.aeroglide.core.model.database.TrackPoint
import com.alpsfly.aeroglide.core.model.database.Calibration
import com.alpsfly.aeroglide.core.model.hardware.SensorData
import com.alpsfly.aeroglide.core.model.hardware.SensorType
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
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

    val trackPointFlow: Flow<TrackPoint>

    val calibration: StateFlow<Calibration>
    fun setCalibration(calibration: Calibration)
    fun resetCalibration()

    // ONE MASTER SWITCH
    fun enableRecordingListeners()
    fun disableRecordingListeners()
}

@Singleton
class SensorRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
    sensorManager: SensorManager,
    fusedLocationProviderClient: FusedLocationProviderClient,
    locationManager: LocationManager,
    @param:SystemTime private val timeProvider: TimeProvider
) : SensorRepository, SensorEventCallback() {

    private val isRecordingListenerEnabled = MutableStateFlow(false)
    override fun enableRecordingListeners() {
        Timber.i("ENABLE ALL RECORDING LISTENERS")
        isRecordingListenerEnabled.value = true
    }

    override fun disableRecordingListeners() {
        Timber.i("DISABLE ALL RECORDING LISTENERS")
        isRecordingListenerEnabled.value = false
    }

    /**
     * Location state flow
     */
    override val locationDataSource = fusedLocationProviderClient.locationDataFlow(
        context = context,
        enable = isRecordingListenerEnabled,
        interval = 1000
    ).shareSensorData()

    @OptIn(FlowPreview::class)
    override val geoidCorrectionDataSource = locationManager.geoidCorrectionFlow(
        context = context,
        enable = isRecordingListenerEnabled,
        interval = 1000
    ).shareSensorData().sample(1000.milliseconds)

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
                    geoidCorrection = geoidCorrection,
                    hasHorizontalAccuracy = l.hasAccuracy(),
                    horizontalAccuracy = l.accuracy,
                    hasVerticalAccuracy = l.hasVerticalAccuracy(),
                    verticalAccuracy = l.verticalAccuracyMeters,
                    bearingAccuracy = l.bearingAccuracyDegrees,
                    speedAccuracy = l.speedAccuracyMetersPerSecond,
                    provider = l.provider ?: "unknown"
                )
            }.logDeviation(
                predicate = { Limits.checkSpeed(it.speed) },
                onDeviation = { Timber.w("Speed out of range: ${it.speed}") }
            ).shareSensorData()
        }

    /**
     * pressure state flow
     */
    override val pressureDataSource = sensorManager.pressureSensorDataFlow(
        enable = isRecordingListenerEnabled
    ).logDeviation(
        predicate = { Limits.checkPressure(it.values[0]) },
        onDeviation = { Timber.w("Pressure out of range: ${it.values[0]}") }
    )

    /**
     * Linear acceleration shared flow
     */
    private val linearAccelerationDataSource = sensorManager.linearAccelerationSensorDataFlow(
        enable = isRecordingListenerEnabled
    )

    /**
     * Rotation vector shared flow
     */
    private val rotationVectorDataSource = sensorManager.rotationVectorSensorDataFlow(
        enable = isRecordingListenerEnabled
    )

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
            }.logDeviation(
                predicate = { Limits.checkVerticalAcceleration(it.values[0]) },
                onDeviation = { Timber.w("Vertical acceleration out of range: ${it.values[0]}") }
            ).shareSensorData()
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
                val pressure = p.values[0]
                var altitude = l.altitude
                if (calibration.value.isCalibrated) {
                    altitude = calcAltitude(
                        pressure,
                        calibration.value.pressure0,
                        calibration.value.altitude0
                    )
                }
                SensorData(
                    type = SensorType.Altitude,
                    timestamp = System.currentTimeMillis(),
                    frequency = sensorFrequency.inc(),
                    values = floatArrayOf(altitude)
                )
            }.logDeviation(
                predicate = { Limits.checkAltitude(it.values[0]) },
                onDeviation = { Timber.w("Altitude out of range: ${it.values[0]}") }
            ).shareSensorData()
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
                // Schritt 1 – Prädiktionsschritt (predict VOR update, korrekter Kalman-Zyklus)
                if (v.timestamp != lastVerticalAcceleration) {
                    if (isKalmanFilterConfigured && sensorFrequency.get() > 0) {
                        kalmanFilter.predict(v.values[0], 1f / sensorFrequency.get())
                    }
                    lastVerticalAcceleration = v.timestamp
                    sensorFrequency.inc()
                }
                // Schritt 2 – Korrekturschritt
                if (a.timestamp != lastAltitude) {
                    if (lastAltitude == 0L) {
                        kalmanFilter.configure(Q_ACCELERATION, R_ALTITUDE, a.values[0])
                        isKalmanFilterConfigured = true
                    }
                    kalmanFilter.update(a.values[0])
                    lastAltitude = a.timestamp
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
                }.logDeviation(
                    predicate = { Limits.checkClimbrate(it.values[0]) },
                    onDeviation = { Timber.w("Climbrate out of range: ${it.values[0]}") }
                ).shareSensorData()
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

    override val trackPointFlow: Flow<TrackPoint> = combine(locationFlow,
        altitudeFlow,
        pressureDataSource,
        climbRateFlow,
        glideRatioFlow
    ) { location, altitude, pressure, climbRate, glideRatio ->
        TrackPoint(
            timestamp = location.timestamp, // Wir nutzen den GPS-Zeitstempel als Anker
            latitude = location.latitude,
            longitude = location.longitude,
            gpsAltitude = location.altitude,
            bearing = location.bearing,
            speed = location.speed,
            geoidCorrection = location.geoidCorrection,
            hasHorizontalAccuracy = location.hasHorizontalAccuracy,
            horizontalAccuracy = location.horizontalAccuracy,
            hasVerticalAccuracy = location.hasVerticalAccuracy,
            verticalAccuracy = location.verticalAccuracy,
            bearingAccuracy = location.bearingAccuracy,
            speedAccuracy = location.speedAccuracy,
            provider = location.provider,

            // Barometrische & berechnete Daten
            altitude = altitude.values[0],
            pressure = pressure.values[0],
            climbrate = climbRate.values[0],
            glideRatio = glideRatio.values[0],

            // Initialer Sync-Status
            syncState = SyncState.LOCAL
        )
    }.shareSensorData() // Wichtig: Damit nur eine Pipe für alle Observer offen ist

    private fun calcAltitude(pressure: Float, pressure0: Float, altitude0: Float): Float {
        val h0 = altitude0.toDouble() // meter
        val ph = pressure.toDouble() * 100f // pascal
        val p0 = pressure0.toDouble() * 100f // pascal

        /**
         * https://de.wikipedia.org/wiki/Barometrische_Höhenformel
         * Th = 288.15f (15°C)
         * h = (Th/0.0065) * (1.0 - (ph/p0)^(1/5.255))
         **/
        val e = 0.1902949571836346 /* 1 / 5.255 */
        val h = 44330.769 * (1.0 - (ph / p0).pow(e))

        return (h0 + h).toFloat()
    }

    private val _calibration = MutableStateFlow(Calibration())
    override val calibration = _calibration.asStateFlow()
    override fun setCalibration(calibration: Calibration) {
        _calibration.value = calibration
    }

    override fun resetCalibration() {
        _calibration.value = Calibration()
    }

    private fun <T> Flow<T>.shareSensorData(stopTimeoutMillis: Long = 5000): Flow<T> = shareIn(
        scope = applicationScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
        replay = 1
    )
}




