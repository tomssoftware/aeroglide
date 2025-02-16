package com.alpsfly.aeroglide.feature.livetracking

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotationGroup
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.AnnotationType
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location

//
//@Composable
//fun LocationPathScreen() {
//    val mapViewportState = rememberMapViewportState()
//    MapboxMap(
//        Modifier.fillMaxSize(),
//        mapViewportState = mapViewportState,
//    ) {
//        MapEffect(Unit) { mapView ->
//            mapView.location.updateSettings {
//                locationPuck = createDefault2DPuck(withBearing = true)
//                enabled = true
//                puckBearing = PuckBearing.COURSE
//                puckBearingEnabled = true
//            }
//            mapViewportState.transitionToFollowPuckState()
//        }
//    }
//}

private const val TRACK_SOURCE_ID = "track-source-id"
private const val TRACK_LAYER_ID = "track-layer-id"
private const val ROUTE_SOURCE_ID = "route-source-id"
private const val ROUTE_LAYER_ID = "route-layer-id"

@Composable
fun LocationPathScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: LocationPathViewModel = hiltViewModel(),
) {
//    val featureList = remember { mutableStateListOf<Feature>() }
//    val featureCollection = remember { mutableStateOf(FeatureCollection.fromFeatures(featureList)) }
    val featureCollectionState = viewModel.mapboxFeatureCollectionFlow.collectAsState()
    val pointCollectionState = viewModel.mapboxPointCollection.collectAsState()

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
//            mapView.mapboxMap.loadStyle(mapStyleContract) { style ->
//                val source = style.getSourceAs<GeoJsonSource>(TRACK_SOURCE_ID)
//                source?.featureCollection(featureCollectionState.value)
//            }
            mapView.location.updateSettings {
                locationPuck = createDefault2DPuck(withBearing = true)
                enabled = true
                puckBearing = PuckBearing.COURSE
                puckBearingEnabled = true
            }
            mapViewportState.transitionToFollowPuckState()
        }

        PolylineAnnotation(
            points = pointCollectionState.value,
        )
        PolylineAnnotationGroup(
            annotations = mutableListOf<PolylineAnnotationOptions>().apply {
                add(
                    PolylineAnnotationOptions()
                        .withPoints(pointCollectionState.value)
                        .withLineColor("#00FF00")
                )
            },
            annotationConfig = AnnotationConfig(
                PITCH_OUTLINE,
                TRACK_LAYER_ID,
                TRACK_SOURCE_ID
            )
        )
    }
}

val POLYLINE_POINTS = listOf(
    Point.fromLngLat(9.126868, 48.370803),
    Point.fromLngLat(9.129370, 48.371289),
    Point.fromLngLat(9.132767, 48.371749),
)

private const val LAYER_ID = "line_layer"
private const val SOURCE_ID = "line_source"
private const val PITCH_OUTLINE = "pitch-outline"
