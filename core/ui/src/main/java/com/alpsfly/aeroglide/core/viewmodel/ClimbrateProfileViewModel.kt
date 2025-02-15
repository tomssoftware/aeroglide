package com.alpsfly.aeroglide.core.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.AppRepository
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.model.database.Altitude
import com.alpsfly.aeroglide.core.model.database.Climbrate
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class ClimbrateProfileViewModel @Inject constructor(
    appRepository: AppRepository,
    sensorRepository: SensorRepository,
) : ViewModel() {

    private val climbrateFlow = sensorRepository.climbrateFlowUi
    private val isRecording = appRepository.isRecording
    private val climbrateChartFlow = combine(climbrateFlow, isRecording) { climbrate, isRecording ->
        ClimbrateChartData(climbrate, isRecording)
    }

    init {
        viewModelScope.launch {
            collectClimbrate()
        }
    }

    private val climbratePoints = mutableStateListOf<Pair<Int, Float>>()
    val climbrateModelProducer = CartesianChartModelProducer()
    private suspend fun collectClimbrate() {
        climbrateChartFlow.collect { climbrateChartData ->
            climbratePoints.add(Pair(climbratePoints.size, climbrateChartData.climbrate.climbrate))
            if (climbrateChartData.isRecording.not()) {
                climbratePoints.clear()
            } else {
                climbrateModelProducer.runTransaction {
                    lineSeries {
                        series(
                            x = climbratePoints.map { it.first },
                            y = climbratePoints.map { it.second }
                        )
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.i("CLEARED VIEWMODEL")
    }
}

data class ClimbrateChartData(
    val climbrate: Climbrate,
    val isRecording: Boolean
)