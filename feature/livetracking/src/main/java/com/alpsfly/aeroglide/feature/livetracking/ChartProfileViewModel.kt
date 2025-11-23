package com.alpsfly.aeroglide.feature.livetracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.domain.usecase.state.AppState
import com.alpsfly.aeroglide.core.domain.usecase.state.AppStateManager
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber

// 1. DEFINE THE GENERIC UI STATE
sealed interface ChartProfileUiState {
    data class Initial(val points: List<Pair<Long, Float>>) : ChartProfileUiState
    data class HasData(
        val points: List<Pair<Long, Float>>,
        val minValue: Float,
        val maxValue: Float
    ) : ChartProfileUiState
}

/**
 * A generic, reusable ViewModel for displaying live and historic chart data.
 * @param T The type of the raw sensor data object from the repository (e.g., Altitude, Climbrate).
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class ChartProfileViewModel<T>(
    appStateManager: AppStateManager,
    protected val dataRepository: DataRepository,
    private val liveDataFlow: Flow<T>,
    private val valueExtractor: (T) -> Float
) : ViewModel() {

    // --- Configuration (to be provided by subclasses) ---
    companion object {
        private const val MAX_LIVE_POINTS = 300 // Keep last 5 mins of live data
        private val INITIAL_POINTS = (0L..10L).map { it to 0f }
    }

    /** A function to load the full historical data for an activity. */
    protected abstract fun loadHistoricData(activityId: Long): Flow<List<Pair<Long, Float>>>

    // --- Shared, Reusable Logic ---
    val modelProducer = CartesianChartModelProducer()
    val uiState: StateFlow<ChartProfileUiState>

    init {
        Timber.d("init view model")
        // This complex logic is now written ONCE and is completely reusable.
        uiState = appStateManager.appState
            .flatMapLatest { state ->
                when (state) {
                    is AppState.Recording -> {
                        streamLiveData(state.activityId)
                    }

                    is AppState.Ready -> {
                        val previousState = state.fromState
                        if (previousState is AppState.Recording) {
                            Timber.d("load historic data for ${previousState.activityId}")
                            loadHistoricData(previousState.activityId)
                                .map { points ->
                                    if (points.isEmpty()) {
                                        ChartProfileUiState.Initial(INITIAL_POINTS)
                                    } else {
                                        ChartProfileUiState.HasData(
                                            points = points,
                                            minValue = points.minOfOrNull { it.second } ?: 0f,
                                            maxValue = points.maxOfOrNull { it.second } ?: 0f
                                        )
                                    }
                                }
                        } else {
                            flowOf(ChartProfileUiState.Initial(INITIAL_POINTS))
                        }
                    }

                    else -> flowOf(ChartProfileUiState.Initial(INITIAL_POINTS))
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ChartProfileUiState.Initial(INITIAL_POINTS)
            )

        // This collector is also shared and reusable.
        viewModelScope.launch {
            uiState.collect { state ->
                modelProducer.runTransaction {
                    when (state) {
                        is ChartProfileUiState.Initial -> lineSeries {
                            series(
                                state.points.map { it.first },
                                state.points.map { it.second })
                        }

                        is ChartProfileUiState.HasData -> lineSeries {
                            series(
                                state.points.map { it.first },
                                state.points.map { it.second })
                        }
                    }
                }
            }
        }
    }

    /**
     * Streams live data, but initializes with the last N points from the database
     * so the chart isn't empty when opening the screen during a flight.
     */
    private fun streamLiveData(activityId: Long): Flow<ChartProfileUiState> {
        // 1. Fetch the existing history for this activity ONCE.
        return loadHistoricData(activityId)
            .take(1) // Take only the first emission (current snapshot) and stop listening to DB
            .flatMapConcat { historyPoints ->

                // 2. Prepare the index counter.
                // If we have history, the next index should be size + 1. 
                // We map history to a continuous 0..N index to match the live chart style.
                var index = 0L

                // Filter to keep only the last MAX points to match the live window
                val recentHistory = historyPoints.takeLast(MAX_LIVE_POINTS)

                // Re-map the history to use our local running index
                val mappedHistory = recentHistory.map { pair ->
                    index++ to pair.second
                }

                // 3. Create the starting state for the scan operator
                val initialState: ChartProfileUiState = if (mappedHistory.isNotEmpty()) {
                    val values = mappedHistory.map { it.second }
                    ChartProfileUiState.HasData(
                        points = mappedHistory,
                        minValue = values.minOrNull() ?: 0f,
                        maxValue = values.maxOrNull() ?: 0f
                    )
                } else {
                    ChartProfileUiState.Initial(points = INITIAL_POINTS)
                }

                // 4. Start scanning live data using the populated initial state
                liveDataFlow.scan(initialState) { currentState, newData ->
                    val currentPoints = when (currentState) {
                        is ChartProfileUiState.Initial -> emptyList()
                        is ChartProfileUiState.HasData -> currentState.points
                    }

                    // Add the new point
                    val newPoint = index++ to valueExtractor(newData)
                    val newPoints = (currentPoints + newPoint).takeLast(MAX_LIVE_POINTS)

                    val valuesInWindow = newPoints.map { it.second }
                    ChartProfileUiState.HasData(
                        points = newPoints,
                        minValue = valuesInWindow.minOrNull() ?: 0f,
                        maxValue = valuesInWindow.maxOrNull() ?: 0f
                    )
                }
            }
    }

    // This can be overridden by subclasses if they need a different range padding.
    open val rangeProvider: CartesianLayerRangeProvider = object : CartesianLayerRangeProvider {
        override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
            return when (val state = uiState.value) {
                is ChartProfileUiState.HasData -> (state.minValue - 10).toDouble()
                is ChartProfileUiState.Initial -> 0.0
            }
        }

        override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
            return when (val state = uiState.value) {
                is ChartProfileUiState.HasData -> (state.maxValue + 10).toDouble()
                is ChartProfileUiState.Initial -> 100.0
            }
        }
    }

    override fun onCleared() {
        Timber.d("clear view model")
        super.onCleared()
    }
}
