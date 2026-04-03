package de.tomssoftware.aeroglide.feature.livetracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tomssoftware.aeroglide.core.data.SensorRepository
import de.tomssoftware.aeroglide.core.mapbox.data.mapToFeatureCollection
import de.tomssoftware.aeroglide.core.mapbox.data.zipMapBoxLocations
import de.tomssoftware.aeroglide.core.model.database.TrackPoint
import com.mapbox.geojson.FeatureCollection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Time window for the visible track segment (5 minutes). */
private const val TRACK_WINDOW_MS = 5 * 60 * 1_000L

sealed interface LocationPathUiState {
    data object Loading : LocationPathUiState
    data class Success(
        val trackFeatureCollection: FeatureCollection,
    ) : LocationPathUiState
}

@HiltViewModel
class LocationPathViewModel @Inject constructor(
    private val sensorRepository: SensorRepository,
) : ViewModel() {

    /** Sliding window of recent track points. */
    private val trackPointWindow = mutableListOf<TrackPoint>()

    val uiState: StateFlow<LocationPathUiState> =
        sensorRepository.trackPointFlow
            .map { trkPt ->
                trackPointWindow.add(trkPt)

                // Trim to the last 5 minutes based on timestamp
                val cutoff = trkPt.timestamp - TRACK_WINDOW_MS
                trackPointWindow.removeAll { it.timestamp < cutoff }

                val locations = zipMapBoxLocations(trackPointWindow)
                    .sortedBy { it.timestamp }

                LocationPathUiState.Success(mapToFeatureCollection(locations))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = LocationPathUiState.Loading,
            )
}
