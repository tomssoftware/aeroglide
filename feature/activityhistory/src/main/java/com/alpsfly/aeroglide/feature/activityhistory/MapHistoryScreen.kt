package com.alpsfly.aeroglide.feature.activityhistory

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotationGroup

@Composable
fun MapHistoryScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: MapHistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(9.126798, 48.370779)) // Default center
            zoom(10.0)
            pitch(0.0)
            bearing(0.0)
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Use a 'when' statement to handle each state exhaustively.
        when (val state = uiState) {
            is MapHistoryUiState.Loading -> {
                // Show a loading indicator in the center.
                CircularProgressIndicator()
            }

            is MapHistoryUiState.Error -> {
                // todo: Show a user-friendly error message.
                Text(
                    text = state.message,
                    color = Color.Red
                )
            }

            is MapHistoryUiState.Success -> {
                // Only show the map if the data was loaded successfully.
                MapboxMap(
                    Modifier.fillMaxSize(),
                    mapViewportState = mapViewportState,
                ) {
                    PolylineAnnotationGroup(annotations = state.trackPolyline)
                }
            }
        }
    }
}
