package com.alpsfly.aeroglide.viewmodel

import android.hardware.Sensor
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.data.repository.SensorRepository
import com.alpsfly.aeroglide.data.util.Limits
import com.alpsfly.aeroglide.data.util.SensorData
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.pow

@HiltViewModel
class SensorViewModel @Inject constructor(
    private val sensorRepository: SensorRepository,
) : ViewModel() {
    /** Shared flow each 200ms */
    private val accelerometerSharedFlow = sensorRepository.accelerometerDataFlow.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    /** Shared flow each 200ms */
    private val pressureSharedFlow = sensorRepository.pressureDataFlow.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    /** Shared flow each 1000ms */
    private val locationSharedFlow = sensorRepository.locationDataFlow.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    fun getAccelSenorData(): SharedFlow<SensorData> {
        return accelerometerSharedFlow
    }

    fun getPressureSenorData(): SharedFlow<SensorData> {
        return pressureSharedFlow
    }

    fun getLocation(): SharedFlow<Location> {
        return locationSharedFlow
    }

    fun getAltitude(): Flow<Float> {
        return altitudeFlow.transform {
            it.values[0]
        }
    }

    private val altitudeFlow: Flow<SensorData>
        get() {
            var altitude0 = 0f
            var pressure0 = 0f
            return combine(pressureSharedFlow, locationSharedFlow) { p, l ->
                val pressure = p.values[0] * 100f
                var altitude = l.altitude.toFloat()
                if (l.hasAccuracy() && l.hasVerticalAccuracy() && pressure != 0f) {
                    pressure0 = pressure
                    altitude0 = altitude
                }
                if (altitude0 != 0f && pressure0 != 0f && pressure != 0f) {
                    altitude = calcAltitude(pressure, pressure0, altitude0)
                }
                Timber.v("Default altitude: ${l.altitude.toFloat()}, ${p.values[0]} -> $altitude")
                SensorData(p.timestamp, floatArrayOf(altitude), Sensor.TYPE_PRESSURE)
            }
            //return flowOf(SensorData(0, floatArrayOf(0f), 0))
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

    private val queue = ArrayDeque<FloatEntry>()
    val chartEntryModelProducer = ChartEntryModelProducer(queue)
    private suspend fun updatePressureGraph() {
        var counter = 1L
        pressureSharedFlow.collect { sensorData ->
            if (queue.size == 10) {
                queue.removeFirst()
            }
            queue.addLast(FloatEntry(counter.toFloat(), sensorData.values[0]))
            chartEntryModelProducer.setEntries(queue)
            counter++
        }
    }

    private val altitudeQueue = ArrayDeque<FloatEntry>()
    val altitudeChartEntryModelProducer = ChartEntryModelProducer(altitudeQueue)
    private suspend fun updateAltitudeGraph() {
        var counter = 1L
        altitudeFlow.collect { sensorData ->
            if (altitudeQueue.size == 10) {
                altitudeQueue.removeFirst()
            }
            altitudeQueue.addLast(FloatEntry(counter.toFloat(), sensorData.values[0]))
            altitudeChartEntryModelProducer.setEntries(altitudeQueue)
            counter++
        }
    }

    init {
        Timber.d("init ${queue.size}")
        viewModelScope.launch {
            updatePressureGraph()
        }
        viewModelScope.launch {
            updateAltitudeGraph()
        }
    }
}
