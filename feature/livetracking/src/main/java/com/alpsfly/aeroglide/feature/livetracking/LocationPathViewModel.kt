package com.alpsfly.aeroglide.feature.livetracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.mapbox.data.mapToFeatureCollection
import com.alpsfly.aeroglide.core.mapbox.data.zipMapBoxLocations
import com.alpsfly.aeroglide.core.model.database.Climbrate
import com.alpsfly.aeroglide.core.model.database.Location
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationPathViewModel @Inject constructor(
    private val sensorRepository: SensorRepository,
) : ViewModel() {

    private var _mapboxFeatureCollection = FeatureCollection.fromFeatures(emptyList())
    val mapboxFeatureCollection: FeatureCollection
        get() {
            return _mapboxFeatureCollection
        }

    private val _mapboxFeatureCollectionFlow = MutableStateFlow<FeatureCollection>(FeatureCollection.fromFeatures(emptyList()))
    val mapboxFeatureCollectionFlow: StateFlow<FeatureCollection> = _mapboxFeatureCollectionFlow

    private val _mapboxPointCollection = MutableStateFlow<List<Point>>(emptyList())
    val mapboxPointCollection: StateFlow<List<Point>> = _mapboxPointCollection

    fun loadFeatureCollection() {
        viewModelScope.launch {
            getMapboxFeatureCollection().collect { featureCollection ->
                _mapboxFeatureCollection = featureCollection
                _mapboxFeatureCollectionFlow.value = featureCollection
            }
        }

        viewModelScope.launch {
            getMapboxPointCollection().collect { pointCollection ->
                _mapboxPointCollection.value = pointCollection
            }
        }
    }

    private fun getMapboxFeatureCollection(): Flow<FeatureCollection> {
        val locations = sensorRepository.locationFlowUi
        val climbrates = sensorRepository.climbrateFlowUi
        val locationList = mutableListOf<Location>()
        val climbrateList = mutableListOf<Climbrate>()

        locationList.clear()
        climbrateList.clear()
        return combine(locations, climbrates) { l, c ->
            locationList.add(l)
            climbrateList.add(c)
            mapToFeatureCollection(zipMapBoxLocations(locationList, climbrateList).sortedBy { it.timestamp })
        }
    }

    private fun getMapboxPointCollection(): Flow<List<Point>> {
        val locations = sensorRepository.locationFlowUi
        val climbrates = sensorRepository.climbrateFlowUi
        val locationList = mutableListOf<Location>()
        val climbrateList = mutableListOf<Climbrate>()

        locationList.clear()
        climbrateList.clear()
        return combine(locations, climbrates) { l, c ->
            locationList.add(l)
            climbrateList.add(c)
            zipMapBoxLocations(locationList, climbrateList)
                .sortedBy { it.timestamp }
                .map { it.point }
        }
    }
}