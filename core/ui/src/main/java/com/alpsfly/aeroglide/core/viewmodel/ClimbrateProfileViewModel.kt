package com.alpsfly.aeroglide.core.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds


@HiltViewModel
class ClimbrateProfileViewModel @Inject constructor(
    sensorRepository: SensorRepository,
) : ViewModel() {

    private val climbrateFlow = sensorRepository.climbrateFlowUi
    private val climbratePoints = mutableStateListOf<Pair<Int, Float>>()
    val climbrateModelProducer = CartesianChartModelProducer()

    init {
        viewModelScope.launch {
            collectClimbrate()
        }
    }

    private suspend fun collectClimbrate() {
        climbrateFlow.collect { climbrate ->
            climbratePoints.add(Pair(climbratePoints.size, climbrate.climbrate))
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