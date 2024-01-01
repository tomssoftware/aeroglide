package com.alpsfly.aeroglide.feature.analogvario

import androidx.lifecycle.ViewModel
import com.alpsfly.aeroglide.core.common.accumulate
import com.alpsfly.aeroglide.core.common.hardware.SensorData
import com.alpsfly.aeroglide.core.common.hardware.SensorType
import com.alpsfly.aeroglide.core.data.repository.SensorRepository
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class AnalogVarioViewModel @Inject constructor(
    private val sensorRepository: SensorRepository,
) : ViewModel() {
    // Physical acceleration sensor
    val acceleration = sensorRepository.accelerometerDataSource.accumulate(1000.milliseconds) { list ->
        val size = list.size.toFloat()
        val values = floatArrayOf(
            list.sumOf { it.values[0].toDouble() }.toFloat() / size,
            list.sumOf { it.values[1].toDouble() }.toFloat() / size,
            list.sumOf { it.values[2].toDouble() }.toFloat() / size
        )
        SensorData(
            timestamp = System.currentTimeMillis(),
            frequency = 1f,
            values = values,
            type = SensorType.Acceleration
        )
    }

    // Physical pressure sensor
    val pressure = sensorRepository.pressureDataSource.accumulate(1000.milliseconds) { list ->
        val value = list.sumOf { it.values[0].toDouble() }.toFloat() / list.size.toFloat()
        SensorData(
            timestamp = System.currentTimeMillis(),
            frequency = 1f,
            values = floatArrayOf(value),
            type = SensorType.Pressure
        )
    }

    // Physical location sensor
    val location = sensorRepository.locationDataSource

    // Fused vertical acceleration sensor
    val verticalAcceleration = sensorRepository.verticalAccelerationFlow.accumulate(1000.milliseconds) { list ->
        val value = list.sumOf { it.values[0].toDouble() }.toFloat() / list.size.toFloat()
        SensorData(
            timestamp = System.currentTimeMillis(),
            frequency = 1f,
            values = floatArrayOf(value),
            type = SensorType.VerticalAcceleration
        )
    }

    // Fused altitude sensor
    val altitude = sensorRepository.altitudeFlow.accumulate(1000.milliseconds) { list ->
        val value = list.sumOf { it.values[0].toDouble() }.toFloat() / list.size.toFloat()
        SensorData(
            timestamp = System.currentTimeMillis(),
            frequency = 1f,
            values = floatArrayOf(value),
            type = SensorType.Altitude
        )
    }

    // Fused climbrate sensor
    val climbrate = sensorRepository.climbRateFlow.accumulate(1000.milliseconds) { list ->
        val value = list.sumOf { it.values[0].toDouble() }.toFloat() / list.size.toFloat()
        SensorData(
            timestamp = System.currentTimeMillis(),
            frequency = 1f,
            values = floatArrayOf(value),
            type = SensorType.Climbrate
        )
    }

    // Update pressure chart
    private val pressureQueue = ArrayDeque<FloatEntry>()
    val pressureChartEntryModelProducer = ChartEntryModelProducer(pressureQueue)
    private suspend fun updatePressureGraph() {
        var counter = 1L
        pressure.collect { sensorData ->
            if (pressureQueue.size == 10) {
                pressureQueue.removeFirst()
            }
            pressureQueue.addLast(FloatEntry(counter.toFloat(), sensorData.values[0]))
            pressureChartEntryModelProducer.setEntries(pressureQueue)
            counter++
        }
    }

    // Update altitude chart
    private val altitudeQueue = ArrayDeque<FloatEntry>()
    val altitudeChartEntryModelProducer = ChartEntryModelProducer(altitudeQueue)
    private suspend fun updateAltitudeGraph() {
        var counter = 1L
        altitude.collect { altitude ->
            if (altitudeQueue.size == 10) {
                altitudeQueue.removeFirst()
            }
            altitudeQueue.addLast(FloatEntry(counter.toFloat(), altitude.values[0]))
            altitudeChartEntryModelProducer.setEntries(altitudeQueue)
            counter++
        }
    }

    // update climbrate chart
    private val climbrateQueue = ArrayDeque<FloatEntry>()
    val climbrateChartEntryModelProducer = ChartEntryModelProducer(climbrateQueue)
    private suspend fun updateClimbrateGraph() {
        var counter = 1L
        climbrate.collect { climbrate ->
            if (climbrateQueue.size == 10) {
                climbrateQueue.removeFirst()
            }
            climbrateQueue.addLast(FloatEntry(counter.toFloat(), climbrate.values[0]))
            climbrateChartEntryModelProducer.setEntries(climbrateQueue)
            counter++
        }
    }

    init {
//        viewModelScope.launch {
//            updatePressureGraph()
//        }
//        viewModelScope.launch {
//            updateAltitudeGraph()
//        }
//        viewModelScope.launch {
//            updateClimbrateGraph()
//        }
    }
}