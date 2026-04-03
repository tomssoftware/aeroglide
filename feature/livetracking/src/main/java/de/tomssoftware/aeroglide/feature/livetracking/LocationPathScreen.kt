package de.tomssoftware.aeroglide.feature.livetracking

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import kotlinx.coroutines.awaitCancellation

private const val TRACK_SOURCE_ID = "track-source"
private const val TRACK_LAYER_ID = "track-layer"

@Composable
fun LocationPathScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: LocationPathViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // rememberUpdatedState: immer aktueller Wert, auch in Callbacks aus MapEffect(Unit)
    val currentUiState by rememberUpdatedState(uiState)
    var trackingEnabled by remember { mutableStateOf(true) }
    var zoomLevel by remember { mutableDoubleStateOf(10.0) }

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(9.126798, 48.370779))
            zoom(zoomLevel)
            pitch(0.0)
            bearing(0.0)
        }
    }

    Box(modifier.fillMaxSize()) {
        MapboxMap(
            Modifier.fillMaxSize(),
            mapViewportState = mapViewportState,
        ) {
            // One-time setup: location puck, gestures, camera + track-layer lifecycle
            MapEffect(Unit) { mapView ->
                mapView.location.updateSettings {
                    locationPuck = createDefault2DPuck(withBearing = true)
                    enabled = true
                    puckBearing = PuckBearing.COURSE
                    puckBearingEnabled = true
                }
                if (trackingEnabled) {
                    mapViewportState.transitionToFollowPuckState()
                }
                mapView.camera.addCameraZoomChangeListener {
                    zoomLevel = it
                }
                mapView.gestures.addOnMoveListener(object : OnMoveListener {
                    override fun onMove(detector: MoveGestureDetector): Boolean = false
                    override fun onMoveBegin(detector: MoveGestureDetector) {
                        trackingEnabled = false
                    }
                    override fun onMoveEnd(detector: MoveGestureDetector) { }
                })

                // Lokale Helper-Funktion: Source + Layer anlegen und ggf. aktuelle
                // Trackdaten sofort einspielen (wichtig nach Style-Reload).
                fun Style.setupTrackLayer() {
                    if (!styleSourceExists(TRACK_SOURCE_ID)) {
                        addSource(
                            geoJsonSource(TRACK_SOURCE_ID) {
                                featureCollection(FeatureCollection.fromFeatures(emptyList()))
                            },
                        )
                        addLayer(
                            lineLayer(TRACK_LAYER_ID, TRACK_SOURCE_ID) {
                                lineColor(Expression.get(Expression.literal("color")))
                                lineWidth(5.0)
                                lineCap(LineCap.ROUND)
                                lineJoin(LineJoin.ROUND)
                            },
                        )
                    }
                    // Aktuellen Track sofort anzeigen (z. B. nach Style-Reload)
                    (currentUiState as? LocationPathUiState.Success)?.let { state ->
                        getSourceAs<GeoJsonSource>(TRACK_SOURCE_ID)
                            ?.featureCollection(state.trackFeatureCollection)
                    }
                }

                // Einmalig für den bereits geladenen Style (fired synchron auf Main-Thread)
                mapView.mapboxMap.getStyle { style -> style.setupTrackLayer() }

                // Bei jedem zukünftigen Style-Reload (z. B. Hintergrund/Vordergrund)
                // wird Source + Layer neu aufgebaut und die aktuellen Trackdaten
                // wieder eingespielt – kein getStyle-Callback-Aufstau möglich, da dies
                // nur einmal registriert wird.
                val cancelable = mapView.mapboxMap.subscribeStyleLoaded {
                    mapView.mapboxMap.style?.setupTrackLayer()
                }

                try {
                    awaitCancellation() // hält den Effect am Leben bis zur Disposal
                } finally {
                    cancelable.cancel()
                }
            }

            // Track-Daten-Update: SYNCHRONER Zugriff – kein getStyle { }-Callback,
            // kein Aufstau. Wenn style == null (Style lädt gerade), wird übersprungen;
            // setupTrackLayer() spielt die Daten beim StyleLoaded-Event nach.
            when (val state = uiState) {
                is LocationPathUiState.Loading -> { /* waiting for first track point */ }
                is LocationPathUiState.Success -> {
                    MapEffect(state.trackFeatureCollection) { mapView ->
                        mapView.mapboxMap.style
                            ?.getSourceAs<GeoJsonSource>(TRACK_SOURCE_ID)
                            ?.featureCollection(state.trackFeatureCollection)
                    }
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
                        followPuckViewportStateOptions = options,
                    )
                },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd),
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Re-center map")
            }
        }
    }
}




