package com.alpsfly.aeroglide.feature.activityhistory

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.mapsforge.core.graphics.Style
import org.mapsforge.core.model.LatLong
import org.mapsforge.core.model.Point
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.android.util.AndroidUtil
import org.mapsforge.map.android.view.MapView
import org.mapsforge.map.datastore.MapDataStore
import org.mapsforge.map.layer.overlay.Polyline
import org.mapsforge.map.layer.renderer.TileRendererLayer
import org.mapsforge.map.reader.MapFile
import org.mapsforge.map.rendertheme.internal.MapsforgeThemes
import java.io.File

@Composable
fun MapHistoryScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: MapHistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Use a 'when' statement to handle each state exhaustively.
        when (val state = uiState) {
            is MapHistoryUiState.Loading -> {
                CircularProgressIndicator()
            }

            is MapHistoryUiState.Downloading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Downloading Map...")
                    LinearProgressIndicator(progress = { state.progress })
                }
            }

            is MapHistoryUiState.Error -> {
                Text(text = state.message, color = Color.Red)
            }

            is MapHistoryUiState.Success -> {
                // Only render the map when the state is Success.
                // All data is guaranteed to be valid and non-null here.
                MapsForgeMapView(
                    mapFile = state.mapFile,
                    trackPoints = state.trackPoints,
                    startPosition = state.startPosition
                )
            }
        }
    }
}

@Composable
private fun MapsForgeMapView(
    mapFile: File,
    trackPoints: List<Point>,
    startPosition: Point?
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            MapView(context).apply {
                model.displayModel.setFixedTileSize(256)
                setZoomLevel(12.toByte())
            }
        },
        update = { mapView ->
            mapView.layerManager.layers.clear()

            val tileCache = AndroidUtil.createTileCache(
                mapView.context,
                "mapcache",
                mapView.model.displayModel.tileSize,
                1f,
                mapView.model.frameBufferModel.overdrawFactor
            )
            val mapDataStore: MapDataStore = MapFile(mapFile)
            val tileRendererLayer = TileRendererLayer(
                tileCache,
                mapDataStore,
                mapView.model.mapViewPosition,
                AndroidGraphicFactory.INSTANCE
            )
            tileRendererLayer.setXmlRenderTheme(MapsforgeThemes.HILLSHADING)

            val paint = AndroidGraphicFactory.INSTANCE.createPaint().apply {
                this.color = AndroidGraphicFactory.INSTANCE.createColor(org.mapsforge.core.graphics.Color.BLUE)
                this.strokeWidth = 8f
                this.setStyle(Style.STROKE)
            }

            val polyline = Polyline(paint, AndroidGraphicFactory.INSTANCE).apply {
                addPoints(trackPoints.map { LatLong(it.y, it.x) })
            }

//            val flightPathLayer = FlightPathLayer()
//            // 2. Populate it with data using zipWithNext to create segments.
//            trackPoints.zipWithNext { start, end ->
//                flightPathLayer.addPolyLine(start, end)
//            }

            mapView.layerManager.layers.add(tileRendererLayer)
            mapView.layerManager.layers.add(polyline)

            if (mapView.model.mapViewPosition.center.latitude == 0.0) {
                startPosition?.let {
                    mapView.setCenter(LatLong(it.y, it.x))
                } ?: mapView.setCenter(mapDataStore.startPosition())
            }

            mapView.invalidate()
        },
        onRelease = { mapView ->
            mapView.destroyAll()
        }
    )
}
