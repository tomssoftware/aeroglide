package com.alpsfly.aeroglide.core.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.AppRepository
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.model.database.Altitude
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AltitudeProfileViewModel @Inject constructor(
    appRepository: AppRepository,
    sensorRepository: SensorRepository,
    private val dataRepository: DataRepository
) : ViewModel() {

    private val altitudeFlow = sensorRepository.altitudeFlowUi
    private val activityId = appRepository.activityId
    private val isRecording = appRepository.isRecording
    private val altitudeChartFlow = combine(altitudeFlow, isRecording) { altitude, isRecording ->
        AltitudeChartData(altitude, isRecording)
    }

    private var minAltitude = 0.0
    private var maxAltitude = 1.0
    val rangeProvider =
        object : CartesianLayerRangeProvider {
            override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore) = minAltitude - 10.0
            override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore) = maxAltitude + 10.0
        }

    init {
        viewModelScope.launch {
            collectAltitude()
        }
        viewModelScope.launch {
            collectActivity()
        }
    }

    private suspend fun collectActivity() {
        activityId.collect { activityId ->
            if (isRecording.value.not())
                return@collect

            dataRepository.getActivityFlow(activityId).collect { activity ->
                if (activity.maxAltitude != Float.MIN_VALUE) {
                    maxAltitude = activity.maxAltitude.toInt().toDouble()
                }
                if (activity.minAltitude != Float.MAX_VALUE) {
                    minAltitude = activity.minAltitude.toInt().toDouble()
                }
            }
        }
    }

    val altitudeModelProducer = CartesianChartModelProducer()
    private val altitudePoints = mutableStateListOf<Pair<Int, Float>>()
    private suspend fun collectAltitude() {
        altitudeChartFlow.collect { altitudeChartData ->
            altitudePoints.add(Pair(altitudePoints.size, altitudeChartData.altitude.altitude))
            if (altitudeChartData.isRecording.not()) {
                altitudePoints.clear()
            } else {
                altitudeModelProducer.runTransaction {
                    lineSeries {
                        series(
                            x = altitudePoints.map { it.first },
                            y = altitudePoints.map { it.second }
                        )
                    }
                }
            }
        }
    }
}

data class AltitudeChartData(
    val altitude: Altitude,
    val isRecording: Boolean
)