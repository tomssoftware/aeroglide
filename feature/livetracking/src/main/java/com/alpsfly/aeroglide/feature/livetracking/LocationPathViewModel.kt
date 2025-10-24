package com.alpsfly.aeroglide.feature.livetracking

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.mapbox.data.MapBoxLocation
import com.alpsfly.aeroglide.core.mapbox.data.zipMapBoxLocations
import com.alpsfly.aeroglide.core.model.database.Climbrate
import com.alpsfly.aeroglide.core.model.database.Location
import com.mapbox.geojson.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationPathViewModel @Inject constructor(
    private val sensorRepository: SensorRepository,
) : ViewModel() {

    private val _zoomLevel = MutableStateFlow(10.0) // Default zoom
    val zoomLevel: StateFlow<Double> = _zoomLevel

    fun setZoomLevel(zoom: Double) {
        _zoomLevel.value = zoom
    }

    private val _isFollowingPuck = MutableStateFlow(true)
    val isFollowingPuck: StateFlow<Boolean> = _isFollowingPuck

    fun setFollowingPuck(follow: Boolean) {
        _isFollowingPuck.value = follow
    }

    private val _mapboxPointCollection = MutableStateFlow<List<Point>>(emptyList())
    val mapboxPointCollection: StateFlow<List<Point>> = _mapboxPointCollection

    private val _mapboxColorCollection = MutableStateFlow<List<Color>>(emptyList())
    val mapboxColorCollection: StateFlow<List<Color>> = _mapboxColorCollection

    fun loadFeatureCollection() {
        viewModelScope.launch {
            getMapboxPointCollection().collect { pointCollection ->
                _mapboxPointCollection.value = pointCollection
            }
        }
        viewModelScope.launch {
            getMapboxColorCollection().collect { colorCollection ->
                _mapboxColorCollection.value = colorCollection
            }
        }
    }

    private fun getMapboxLocationCollection(): Flow<List<MapBoxLocation>> {
        val locations = sensorRepository.locationFlowUi
        val climbrates = sensorRepository.climbrateFlowUi
        val locationList = mutableListOf<Location>()
        val climbrateList = mutableListOf<Climbrate>()

        locationList.clear()
        climbrateList.clear()
        return combine(locations, climbrates) { l, c ->
            locationList.add(l)
            climbrateList.add(c)

            if (locationList.size > 120) {
                locationList.removeAt(0)
            }
            if (climbrateList.size > 120) {
                climbrateList.removeAt(0)
            }

            zipMapBoxLocations(locationList, climbrateList)
                .sortedBy { it.timestamp }
        }
    }

    private fun getMapboxPointCollection(): Flow<List<Point>> {
        return getMapboxLocationCollection().map {
            it.map { mapBoxLocation ->
                mapBoxLocation.point
            }
        }
    }

    private fun getMapboxColorCollection(): Flow<List<Color>> {
        return getMapboxLocationCollection().map {
            it.map { mapBoxLocation ->
                Color(mapBoxLocation.color.toColorInt())
            }
        }
    }
}