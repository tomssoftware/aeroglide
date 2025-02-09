package com.alpsfly.aeroglide.core.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AltitudeProfileViewModel @Inject constructor(
    sensorRepository: SensorRepository,
    dataRepository: DataRepository
) : ViewModel() {

    private val altitudeFlow = sensorRepository.altitudeFlowUi
    private val activityFlow = dataRepository.activityFlow

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
        activityFlow.collect { activity ->
            if (activity.maxAltitude != Float.MIN_VALUE) {
                maxAltitude = activity.maxAltitude.toInt().toDouble()
            }
            if (activity.minAltitude != Float.MAX_VALUE) {
                minAltitude = activity.minAltitude.toInt().toDouble()
            }
        }
    }

    val altitudeModelProducer = CartesianChartModelProducer()
    private val altitudePoints = mutableStateListOf<Pair<Int, Float>>()
    private suspend fun collectAltitude() {
        altitudeFlow.collect { altitude ->
            altitudePoints.add(Pair(altitudePoints.size, altitude.altitude))
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