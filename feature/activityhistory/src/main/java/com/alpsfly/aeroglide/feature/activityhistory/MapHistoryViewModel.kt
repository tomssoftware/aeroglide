package com.alpsfly.aeroglide.feature.activityhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.AppRepository
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.model.configuration.ColorMapping
import com.mapbox.geojson.FeatureCollection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.alpsfly.aeroglide.core.mapbox.data.mapToFeatureCollection
import com.alpsfly.aeroglide.core.mapbox.data.zipMapBoxLocations
import com.mapbox.geojson.Point

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

    fun loadFeatureCollection(activityId: Long) {
        viewModelScope.launch {
            val activity = dataRepository.getActivity(activityId)
            getMapboxFeatureCollection(activity.begin, activity.end).collect { featureCollection ->
                _mapboxFeatureCollection = featureCollection
            }
        }
    }

    private fun getMapboxFeatureCollection(begin: Long, end: Long): Flow<FeatureCollection> {
        val locations = dataRepository.getLocationsBetween(begin, end)
        val climbrates = dataRepository.getClimbratesBetween(begin, end)
        return combine(locations, climbrates) { l, c ->
            mapToFeatureCollection(zipMapBoxLocations(l, c).sortedBy { it.timestamp })
        }
    }
}
