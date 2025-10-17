package com.alpsfly.aeroglide.feature.livetracking

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.AppRepository
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.viewmodel.LineChartViewModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AltitudeProfileViewModel @Inject constructor(
    appRepository: AppRepository,
    sensorRepository: SensorRepository,
    dataRepository: DataRepository
) : LineChartViewModel(appRepository, dataRepository) {

    private val altitudeFlow = sensorRepository.altitudeFlowUi

    val rangeProvider =
        object : CartesianLayerRangeProvider {
            override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore) = minAltitude - 10.0
            override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore) = maxAltitude + 10.0
        }

    val altitudeModelProducer = CartesianChartModelProducer()
    private val altitudePoints = mutableStateListOf<Pair<Int, Float>>()

    init {
        viewModelScope.launch {
            altitudeModelProducer.runTransaction {
                lineSeries { series(0, 0, 0, 0, 0, 0, 0, 0, 0, 0) }
            }
        }
        viewModelScope.launch {
            collectAltitude()
        }
    }

    private suspend fun collectAltitude() {
        altitudeFlow.collect { altitudeChartData ->
            altitudePoints.add(Pair(altitudePoints.size, altitudeChartData.altitude))
            if (isRecording.value) {
                altitudeModelProducer.runTransaction {
                    lineSeries {
                        series(
                            x = altitudePoints.map { it.first },
                            y = altitudePoints.map { it.second }
                        )
                    }
                }
            } else {
                altitudePoints.clear()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.i("CLEARED VIEWMODEL")
    }
}
