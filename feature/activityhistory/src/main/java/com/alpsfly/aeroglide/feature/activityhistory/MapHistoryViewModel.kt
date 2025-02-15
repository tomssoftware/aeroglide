package com.alpsfly.aeroglide.feature.activityhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.AppRepository
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.model.configuration.ColorMapping
import com.alpsfly.aeroglide.core.model.database.Climbrate
import com.alpsfly.aeroglide.core.model.database.Location
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapHistoryViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val dataRepository: DataRepository,
) : ViewModel() {

    private var colorMapping = ColorMapping()
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

    private fun zipMapBoxLocations(locations: List<Location>, climbrates: List<Climbrate>): List<MapBoxLocation> {
        var pathSegment = 0
        var lastColor = ""
        return locations.zip(climbrates).map { (location, climbrate) ->
            val color = colorMapping.getClimbrateColor(climbrate.climbrate)
            if (color != lastColor) {
                lastColor = color
                pathSegment++
            }

            MapBoxLocation(
                timestamp = location.timestamp,
                segment = pathSegment,
                point = Point.fromLngLat(location.longitude.toDouble(), location.latitude.toDouble()),
                color = color
            )
        }
    }

    private fun mapToFeatureCollection(locations: List<MapBoxLocation>): FeatureCollection {
        val featureList = mutableListOf<Feature>()
        val mapBoxLocationGroups = locations.groupBy { it.segment }

        var lastLocation: MapBoxLocation? = null
        mapBoxLocationGroups.forEach { (_, locations) ->
            val mutableLocationList = locations.toMutableList()
            val pointList = if (locations.size >= 2) {
                lastLocation?.let {
                    mutableLocationList.add(0, it)
                }
                mutableLocationList.map { it.point }
            } else {
                lastLocation?.let {
                    listOf(it.point, locations[0].point)
                }
            }

            pointList?.let {
                val feature = Feature.fromGeometry(LineString.fromLngLats(it)).apply {
                    this.addStringProperty("color", locations.first().color)
                }
                featureList.add(feature)
            }

            lastLocation = locations.last()
        }
        return FeatureCollection.fromFeatures(featureList)
    }
}

data class MapBoxLocation(
    val timestamp: Long,
    val segment: Int,
    val point: Point,
    val color: String
)