package de.tomssoftware.aeroglide.core.mapbox.data

import de.tomssoftware.aeroglide.core.model.configuration.ColorMapping
import de.tomssoftware.aeroglide.core.model.database.TrackPoint
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point

fun zipMapBoxLocations(trackPointPoints: List<TrackPoint>): List<MapBoxLocation> {
    val colorMapping = ColorMapping()
    var pathSegment = 0
    var lastColor = ""
    return trackPointPoints.map { trkpt ->
        val color = colorMapping.getClimbrateColor(trkpt.climbrate)
        if (color != lastColor) {
            lastColor = color
            pathSegment++
        }

        MapBoxLocation(
            timestamp = trkpt.timestamp,
            segment = pathSegment,
            point = Point.fromLngLat(trkpt.longitude.toDouble(), trkpt.latitude.toDouble()),
            color = color
        )
    }
}

fun mapToFeatureCollection(locations: List<MapBoxLocation>): FeatureCollection {
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

data class MapBoxLocation(
    val timestamp: Long,
    val segment: Int,
    val point: Point,
    val color: String
)