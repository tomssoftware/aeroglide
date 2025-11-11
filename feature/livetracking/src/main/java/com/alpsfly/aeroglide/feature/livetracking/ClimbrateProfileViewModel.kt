package com.alpsfly.aeroglide.feature.livetracking

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.AppRepository
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.domain.usecase.state.AppState
import com.alpsfly.aeroglide.core.domain.usecase.state.AppStateManager
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
class ClimbrateProfileViewModel @Inject constructor(
    appStateManager: AppStateManager,
    appRepository: AppRepository,
    sensorRepository: SensorRepository,
    dataRepository: DataRepository
) : LineChartViewModel(appStateManager, appRepository, dataRepository) {

    private val climbrateFlow = sensorRepository.climbrateFlowUi

    val rangeProvider =
        object : CartesianLayerRangeProvider {
            override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore) =
                minClimbrate - 0.25

            override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore) =
                maxClimbrate + 0.25
        }

    private val climbratePoints = mutableStateListOf<Pair<Int, Float>>()
    val climbrateModelProducer = CartesianChartModelProducer()

    init {
        viewModelScope.launch {
            climbrateModelProducer.runTransaction {
                lineSeries { series(0, 0, 0, 0, 0, 0, 0, 0, 0, 0) }
            }
        }
        viewModelScope.launch {
            collectClimbrate()
        }
    }

    private suspend fun collectClimbrate() {
        climbrateFlow.collect { climbrateChartData ->
            climbratePoints.add(Pair(climbratePoints.size, climbrateChartData.climbrate))
            if (appState.value == AppState.Recording) {
                climbrateModelProducer.runTransaction {
                    lineSeries {
                        series(
                            x = climbratePoints.map { it.first },
                            y = climbratePoints.map { it.second }
                        )
                    }
                }
            } else {
                climbratePoints.clear()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.i("CLEARED VIEWMODEL")
    }
}
