package com.alpsfly.aeroglide.feature.activityhistory

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.extension.style.style

private const val TRACK_SOURCE_ID = "track-source-id"
private const val TRACK_LAYER_ID = "track-layer-id"
private const val ROUTE_SOURCE_ID = "route-source-id"
private const val ROUTE_LAYER_ID = "route-layer-id"

@Composable
fun MapHistoryScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    activityId: Long,
    viewModel: MapHistoryViewModel = hiltViewModel(),
) {
    LaunchedEffect(key1 = activityId) {
        viewModel.loadFeatureCollection(activityId)
    }

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(9.05419, 48.40567))
            zoom(10.0)
            pitch(0.0)
            bearing(0.0)
        }
    }

    val mapStyleContract = style(style = Style.OUTDOORS) {
        +geoJsonSource(id = TRACK_SOURCE_ID) {
            // url("asset://empty-track.geojson")
        }
        +geoJsonSource(id = ROUTE_SOURCE_ID) {
            // url("asset://empty-track.geojson")
        }
        +lineLayer(layerId = TRACK_LAYER_ID, sourceId = TRACK_SOURCE_ID) {
            lineCap(LineCap.ROUND)
            lineJoin(LineJoin.ROUND)
            lineOpacity(0.7)
            lineWidth(8.0)
            lineColor(
                com.mapbox.maps.extension.style.expressions.dsl.generated.get {
                    literal("color")
                }
            )
        }
        +lineLayer(layerId = ROUTE_LAYER_ID, sourceId = ROUTE_SOURCE_ID) {
            lineCap(LineCap.ROUND)
            lineJoin(LineJoin.ROUND)
            lineOpacity(0.4)
            lineWidth(8.0)
            lineColor("#0d47c1")
        }
    }

    MapboxMap(
        Modifier.fillMaxSize(),
        mapViewportState = mapViewportState,
    ) {
        MapEffect(Unit) { mapView ->
            mapView.mapboxMap.loadStyle(mapStyleContract) { style ->
                val source = style.getSourceAs<GeoJsonSource>(TRACK_SOURCE_ID)
                source?.featureCollection(viewModel.mapboxFeatureCollection)
//              val feature = Feature.fromGeometry(LineString.fromLngLats(viewModel.trackPoints))
//              source?.feature(feature)
            }
        }
    }
}
