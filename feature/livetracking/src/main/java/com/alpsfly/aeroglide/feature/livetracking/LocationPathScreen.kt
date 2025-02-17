package com.alpsfly.aeroglide.feature.livetracking

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location

@Composable
fun LocationPathScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: LocationPathViewModel = hiltViewModel(),
) {
    val pointList by viewModel.mapboxPointCollection.collectAsState()
    val colorList by viewModel.mapboxColorCollection.collectAsState()

    LaunchedEffect(key1 = 0) {
        viewModel.loadFeatureCollection()
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
        MapEffect(Unit) { mapView ->
            mapView.location.updateSettings {
                locationPuck = createDefault2DPuck(withBearing = true)
                enabled = true
                puckBearing = PuckBearing.COURSE
                puckBearingEnabled = true
            }
            mapViewportState.transitionToFollowPuckState()
        }
        pointList.zipWithNext().forEachIndexed { index, (start, end) ->
            PolylineAnnotation(
                points = listOf(start, end),
            ) {
                lineColor = colorList.getOrElse(index) { Color.Black } // Default to black if color is not available
                lineWidth = 5.0
            }
        }
    }
}
