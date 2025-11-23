package com.alpsfly.aeroglide.feature.livetracking

import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.domain.usecase.state.AppStateManager
import com.alpsfly.aeroglide.core.model.database.Climbrate
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class ClimbrateProfileViewModel @Inject constructor(
    appStateManager: AppStateManager,
    sensorRepository: SensorRepository,
    dataRepository: DataRepository
) : ChartProfileViewModel<Climbrate>(
    appStateManager = appStateManager,
    dataRepository = dataRepository,
    liveDataFlow = sensorRepository.climbrateFlowUi,
    valueExtractor = { it.climbrate }
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun loadHistoricData(activityId: Long): Flow<List<Pair<Long, Float>>> {
        // Return a Flow that reacts to the activity.
        return dataRepository.getActivityFlow(activityId).flatMapLatest { activity ->
            if (activity == null) {
                // If activity is null, emit an empty list for the chart
                flowOf(emptyList())
            } else {
                // If activity exists, get the altitudes and map them.
                dataRepository.getClimbratesBetween(activity.begin, activity.end).map { climbrates ->
                    val startTime = climbrates.firstOrNull()?.timestamp ?: 0L
                    climbrates.map {
                        val timeDeltaSeconds = (it.timestamp - startTime) / 1000
                        timeDeltaSeconds to it.climbrate
                    }
                }
            }
        }
    }

    // --- OVERRIDE AND PROVIDE SPECIFIC IMPLEMENTATIONS ---

    // Climbrate needs a different y-axis padding
    override val rangeProvider = object : CartesianLayerRangeProvider {
        override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
            return when (val state = uiState.value) {
                is ChartProfileUiState.HasData -> (state.minValue - 0.5).toDouble()
                is ChartProfileUiState.Initial -> -2.0
            }
        }

        override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
            return when (val state = uiState.value) {
                is ChartProfileUiState.HasData -> (state.maxValue + 0.5).toDouble()
                is ChartProfileUiState.Initial -> 2.0
            }
        }
    }

    val yAxisLabelFormatter = CartesianValueFormatter { _, value, _ ->
        "%.1f m/s".format(value)
    }

    // We can reuse the same time formatter as the Altitude chart
    val xAxisLabelFormatter = CartesianValueFormatter { _, value, _ ->
        val totalSeconds = value.toLong()
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        "%02d:%02d".format(minutes, seconds)
    }
}
