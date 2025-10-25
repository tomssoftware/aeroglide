package com.alpsfly.aeroglide.feature.livetracking

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions

@Composable
fun LocationPathScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: LocationPathViewModel = hiltViewModel(),
) {
    val pointList by viewModel.mapboxPointCollection.collectAsState()
    val colorList by viewModel.mapboxColorCollection.collectAsState()
    var trackingEnabled by remember { mutableStateOf(true) }
    var zoomLevel by remember { mutableDoubleStateOf(10.0) }


    LaunchedEffect(key1 = 0) {
        viewModel.loadFeatureCollection()
    }

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(9.126798, 48.370779))
            zoom(zoomLevel)
            pitch(0.0)
            bearing(0.0)
        }
    }

    Box(Modifier.fillMaxSize()) {
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
                // Always start in follow mode
                if (trackingEnabled) {
                    mapViewportState.transitionToFollowPuckState()
                }
                // Remember zoom level
                mapView.camera.addCameraZoomChangeListener {
                    zoomLevel = it
                }
                // Listen for gestures to disable follow mode
                mapView.gestures.addOnMoveListener(object : OnMoveListener {
                    override fun onMove(detector: MoveGestureDetector): Boolean {
                        return false
                    }

                    override fun onMoveBegin(detector: MoveGestureDetector) {
                        trackingEnabled = false
                    }

                    override fun onMoveEnd(detector: MoveGestureDetector) {
                        // no action required
                    }
                })
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
        if (!trackingEnabled) {
            FloatingActionButton(
                onClick = {
                    trackingEnabled = true
                    val options = FollowPuckViewportStateOptions.Builder()
                        .zoom(zoomLevel)
                        .build()
                    mapViewportState.transitionToFollowPuckState(
                        followPuckViewportStateOptions = options
                    )
                },
                modifier = Modifier
                    .padding(16.dp)
                    .align(
                        Alignment.BottomEnd
                    )
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Re-center map")
            }
        }
    }
}


