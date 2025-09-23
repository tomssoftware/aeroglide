package com.alpsfly.aeroglide.feature.activityhistory

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.graphics.toColorInt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotationGroup
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions

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
    val locations by viewModel.mapboxLocationCollection.collectAsState()

    LaunchedEffect(key1 = activityId) {
        viewModel.loadFeatureCollection(activityId)
    }

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(9.126798, 48.370779))
            zoom(10.0)
            pitch(0.0)
            bearing(0.0)
        }
    }

    MapboxMap(
        Modifier.fillMaxSize(),
        mapViewportState = mapViewportState,
    ) {
        PolylineAnnotationGroup(
            annotations = locations.zipWithNext().map { (start, end) ->
                PolylineAnnotationOptions()
                    .withPoints(listOf(start.point, end.point))
                    .withLineColor(start.color.toColorInt())
                    .withLineWidth(5.0)
            }
        )
    }
}
