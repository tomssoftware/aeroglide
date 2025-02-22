package com.alpsfly.aeroglide.feature.activityhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.AppRepository
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.mapbox.data.MapBoxLocation
import com.alpsfly.aeroglide.core.mapbox.data.mapToFeatureCollection
import com.alpsfly.aeroglide.core.mapbox.data.zipMapBoxLocations
import com.mapbox.geojson.FeatureCollection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapHistoryViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val dataRepository: DataRepository,
) : ViewModel() {
    val activityId = appRepository.activityId

    private var _mapboxFeatureCollection = FeatureCollection.fromFeatures(emptyList())
    val mapboxFeatureCollection: FeatureCollection
        get() {
            return _mapboxFeatureCollection
        }

    private var _mapboxLocationCollection = MutableStateFlow<List<MapBoxLocation>>(emptyList())
    val mapboxLocationCollection: StateFlow<List<MapBoxLocation>>
        get() {
            return _mapboxLocationCollection
        }


    fun loadFeatureCollection(activityId: Long) {
        viewModelScope.launch {
            val activity = dataRepository.getActivity(activityId)
            getMapboxFeatureCollection(activity.begin, activity.end).collect { featureCollection ->
                _mapboxFeatureCollection = featureCollection
            }
        }
        viewModelScope.launch {
            val activity = dataRepository.getActivity(activityId)
            getMapboxLocationCollection(activity.begin, activity.end).collect { locationCollection ->
                _mapboxLocationCollection.value = locationCollection
            }
        }
    }

    private fun getMapboxFeatureCollection(begin: Long, end: Long): Flow<FeatureCollection> {
        val locations = dataRepository.getLocationsBetween(begin, end)
        val climbrates = dataRepository.getClimbratesBetween(begin, end)
        return combine(locations, climbrates) { l, c ->
            mapToFeatureCollection(zipMapBoxLocations(l, c)
                .sortedBy { it.timestamp })
        }
    }

    private fun getMapboxLocationCollection(begin: Long, end: Long): Flow<List<MapBoxLocation>> {
        val locations = dataRepository.getLocationsBetween(begin, end)
        val climbrates = dataRepository.getClimbratesBetween(begin, end)
        return combine(locations, climbrates) { l, c ->
            zipMapBoxLocations(l, c)
                .sortedBy { it.timestamp }
        }
    }
}
