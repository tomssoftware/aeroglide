package com.alpsfly.aeroglide.feature.activityhistory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.model.database.Activity
import com.alpsfly.aeroglide.core.model.hardware.Altitude
import com.alpsfly.aeroglide.core.model.hardware.Climbrate
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val dataRepository: DataRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // --- Main Activity Data ---
    val allActivitiesUiState: StateFlow<ActivityListUiState> =
        dataRepository.allActivities
            .map { list -> ActivityListUiState.Success(list) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ActivityListUiState.Loading
            )

    private val activityId: StateFlow<Long> = savedStateHandle.getStateFlow("activityId", 0L)

    val activity: StateFlow<ActivityUiState> = activityId
        .flatMapLatest { id ->
            if (id == 0L) {
                flowOf(ActivityUiState.Loading) // Or an error state
            } else {
                dataRepository.getActivityFlow(id).map { activity ->
                    if (activity != null) ActivityUiState.Success(activity)
                    else ActivityUiState.Loading
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ActivityUiState.Loading
        )

    fun deleteActivity(activity: Activity) {
        viewModelScope.launch {
            dataRepository.deleteActivity(activity)
        }
    }

    // --- Chart Logic ---

    val altitudeModelProducer = CartesianChartModelProducer()
    val climbrateModelProducer = CartesianChartModelProducer()

    // Placeholder data to prevent Vico from crashing on empty series
    private val emptySeriesData = listOf(0L to 0f)

    // 1. Reactive Altitude State
    @OptIn(ExperimentalCoroutinesApi::class)
    // 3. Reactive Altitude State - now driven by `activityId`
    val altitudeUiState: StateFlow<HistoryChartUiState> = activityId
        .flatMapLatest { id ->
            if (id == 0L) return@flatMapLatest flowOf(HistoryChartUiState.Loading)
            createChartFlow(
                activityId = id,
                getDataFlow = { start, end -> dataRepository.getTracksBetween(start, end) },
                extractTimestamp = { it.timestamp },
                extractValue = { it.altitude }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HistoryChartUiState.Loading
        )

    // 4. Reactive Climbrate State - now driven by `activityId`
    val climbrateUiState: StateFlow<HistoryChartUiState> = activityId
        .flatMapLatest { id ->
            if (id == 0L) return@flatMapLatest flowOf(HistoryChartUiState.Loading)
            createChartFlow(
                activityId = id,
                getDataFlow = { start, end -> dataRepository.getTracksBetween(start, end) },
                extractTimestamp = { it.timestamp },
                extractValue = { it.climbrate }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HistoryChartUiState.Loading
        )

    init {
        // 3. Collect States and Update Producers
        viewModelScope.launch {
            altitudeUiState.collect { state -> updateProducer(altitudeModelProducer, state) }
        }
        viewModelScope.launch {
            climbrateUiState.collect { state -> updateProducer(climbrateModelProducer, state) }
        }
    }

    // --- Helper Functions ---

    /**
     * Generic function to create a chart state flow from repository data.
     * This function is now fully self-contained and only depends on activityId.
     */
    private fun <T> createChartFlow(
        activityId: Long,
        getDataFlow: (start: Long, end: Long) -> Flow<List<T>>,
        extractTimestamp: (T) -> Long,
        extractValue: (T) -> Float
    ): Flow<HistoryChartUiState> {
        return dataRepository.getActivityFlow(activityId).flatMapLatest { activity ->
            if (activity == null || activity.begin == 0L || activity.end == 0L) {
                return@flatMapLatest flowOf(HistoryChartUiState.NoData)
            }

            getDataFlow(activity.begin, activity.end).map { list ->
                if (list.isEmpty()) {
                    HistoryChartUiState.NoData
                } else {
                    val startTime = list.firstOrNull()?.let { (it as? Any).getTimestamp() } ?: activity.begin
                    val points = list.map { item ->
                        val timestamp = extractTimestamp(item)
                        val timeDeltaSeconds = (timestamp - startTime) / 1000
                        timeDeltaSeconds to extractValue(item)
                    }

                    HistoryChartUiState.Success(
                        points = points,
                        minValue = points.minOfOrNull { it.second } ?: 0f,
                        maxValue = points.maxOfOrNull { it.second } ?: 0f
                    )
                }
            }
        }
    }

    // This is a helper to generically get a timestamp if the object has one.
    // A more robust solution would be a shared interface like `interface Timestamped { val timestamp: Long }`
    private fun Any?.getTimestamp(): Long {
        return when (this) {
            is Altitude -> this.timestamp
            is Climbrate -> this.timestamp
            else -> 0L
        }
    }

    private suspend fun updateProducer(
        producer: CartesianChartModelProducer,
        state: HistoryChartUiState
    ) {
        producer.runTransaction {
            when (state) {
                is HistoryChartUiState.Success -> {
                    lineSeries {
                        series(
                            x = state.points.map { it.first },
                            y = state.points.map { it.second }
                        )
                    }
                }

                else -> {
                    // Use placeholder data instead of empty lists to avoid "Series can't be empty" crash
                    lineSeries {
                        series(
                            x = emptySeriesData.map { it.first },
                            y = emptySeriesData.map { it.second }
                        )
                    }
                }
            }
        }
    }
    // --- Range Providers ---

    val altitudeRangeProvider = object : CartesianLayerRangeProvider {
        override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
            return when (val state = altitudeUiState.value) {
                is HistoryChartUiState.Success -> (state.minValue - 10.0)
                else -> 0.0
            }
        }

        override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
            return when (val state = altitudeUiState.value) {
                is HistoryChartUiState.Success -> (state.maxValue + 10.0)
                else -> 100.0
            }
        }
    }

    val climbrateRangeProvider = object : CartesianLayerRangeProvider {
        override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
            return when (val state = climbrateUiState.value) {
                is HistoryChartUiState.Success -> (state.minValue - 0.5)
                else -> -2.0
            }
        }

        override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
            return when (val state = climbrateUiState.value) {
                is HistoryChartUiState.Success -> (state.maxValue + 0.5)
                else -> 2.0
            }
        }
    }

    // --- Formatters ---

    val yAxisLabelFormatter = CartesianValueFormatter { context, value, verticalAxisPosition -> "${value.toInt()} m" }

    val yAxisLabelFormatterClimbrate = CartesianValueFormatter { _, value, _ ->
        "%.1f m/s".format(value)
    }

    val xAxisLabelFormatter = CartesianValueFormatter { context, value, verticalAxisPosition ->
        val totalSeconds = value.toLong()
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        when {
            hours > 0 -> "%02d:%02d".format(hours, minutes)
            else -> "%02d:%02d".format(minutes, seconds)
        }
    }
}

// --- Sealed Interfaces ---

sealed interface ActivityUiState {
    data object Loading : ActivityUiState
    data class Success(val item: Activity) : ActivityUiState
}

sealed interface ActivityListUiState {
    data object Loading : ActivityListUiState
    data class Success(val list: List<Activity>) : ActivityListUiState
}

sealed interface HistoryChartUiState {
    data object Loading : HistoryChartUiState
    data object NoData : HistoryChartUiState
    data class Success(
        val points: List<Pair<Long, Float>>,
        val minValue: Float,
        val maxValue: Float
    ) : HistoryChartUiState
}
