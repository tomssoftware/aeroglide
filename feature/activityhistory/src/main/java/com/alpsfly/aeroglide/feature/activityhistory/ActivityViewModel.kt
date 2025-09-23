package com.alpsfly.aeroglide.feature.activityhistory

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.model.configuration.ColorMapping
import com.alpsfly.aeroglide.core.model.database.Activity
import com.mapbox.geojson.Point
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val dataRepository: DataRepository,
) : ViewModel() {



    val allActivitiesUiState: StateFlow<ActivityListUiState> =
        dataRepository.allActivities
            .map { list -> ActivityListUiState.Success(list) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ActivityListUiState.Loading
            )

    private val _activity = MutableStateFlow<ActivityUiState>(ActivityUiState.Loading)
    val activity: StateFlow<ActivityUiState> get() = _activity

    fun loadActivityById(id: Long) {
        viewModelScope.launch {
            _activity.value = ActivityUiState.Loading
            _activity.value = ActivityUiState.Success(
                dataRepository.getActivity(id)
            )
        }
    }

    private var minAltitude = 0.0
    private var maxAltitude = 1.0
    val rangeProvider =
        object : CartesianLayerRangeProvider {
            override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore) = minAltitude - 10.0
            override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore) = maxAltitude + 10.0
        }

    val altitudeModelProducer = CartesianChartModelProducer()
    fun loadAltitudes(activityId: Long) {
        viewModelScope.launch {
            val activity = dataRepository.getActivity(activityId)
            val altitudeFlow = dataRepository.getAltitudesBetween(activity.begin, activity.end)
            val altitudePoints = mutableStateListOf<Pair<Int, Float>>()

            minAltitude = activity.minAltitude.toDouble()
            maxAltitude = activity.maxAltitude.toDouble()

            altitudeFlow.collect { altitudeList ->
                altitudeList.forEach { altitude ->
                    altitudePoints.add(Pair(altitudePoints.size, altitude.altitude))
                }
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

    val climbrateModelProducer = CartesianChartModelProducer()
    fun loadClimbrates(activityId: Long) {
        viewModelScope.launch {
            val activity = dataRepository.getActivity(activityId)
            val climbrateFlow = dataRepository.getClimbratesBetween(activity.begin, activity.end)
            val climbratePoints = mutableStateListOf<Pair<Int, Float>>()

            climbrateFlow.collect { climbrateList ->
                climbrateList.forEach { climbrate ->
                    climbratePoints.add(Pair(climbratePoints.size, climbrate.climbrate))
                }
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

    private val trackPoints = mutableListOf<Point>()
    fun loadLocations(activityId: Long) {
        viewModelScope.launch {
            val activity = dataRepository.getActivity(activityId)
            val locationFlow = dataRepository.getLocationsBetween(activity.begin, activity.end)
            trackPoints.clear()
            locationFlow.collect { locationList ->
                locationList.forEach { location ->
                    trackPoints.add(Point.fromLngLat(location.longitude.toDouble(), location.latitude.toDouble()))
                }
            }
        }
    }

    fun deleteActivity(activity: Activity) {
        viewModelScope.launch {
            dataRepository.deleteActivity(activity)
        }
    }
}

sealed interface ActivityUiState {
    data object Loading : ActivityUiState
    data class Success(
        val item: Activity
    ) : ActivityUiState
}

sealed interface ActivityListUiState {
    data object Loading : ActivityListUiState
    data class Success(
        val list: List<Activity>,
    ) : ActivityListUiState
}