package de.tomssoftware.aeroglide.feature.activityhistory

import androidx.core.graphics.toColorInt
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tomssoftware.aeroglide.core.data.DataRepository
import de.tomssoftware.aeroglide.core.mapbox.data.zipMapBoxLocations
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// This is the single source of truth for the Map History UI.
sealed interface MapHistoryUiState {
    /** The screen is currently loading the track data. */
    data object Loading : MapHistoryUiState

    /** The track data was loaded successfully. */
    data class Success(
        val trackPolyline: List<PolylineAnnotationOptions> = emptyList()
    ) : MapHistoryUiState

    /** An error occurred, e.g., the activity could not be found. */
    data class Error(val message: String) : MapHistoryUiState
}

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class MapHistoryViewModel @Inject constructor(
    private val dataRepository: DataRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val activityId: StateFlow<Long> = savedStateHandle.getStateFlow("activityId", 0L)
    val uiState: StateFlow<MapHistoryUiState> = activityId
        .flatMapLatest { id ->
            dataRepository.getActivityFlow(id).flatMapLatest { activity ->
                if (activity == null) {
                    // If activity is null, return an error flow.
                    flowOf(MapHistoryUiState.Error("Activity not found."))
                } else if (activity.begin == 0L || activity.end == 0L) {
                    // If activity has no data, return a success state with an empty track.
                    flowOf(MapHistoryUiState.Success(trackPolyline = emptyList()))
                } else {
                    // If activity is valid, combine the data sources to build the track.
                    dataRepository.getTracksBetween(activity.begin, activity.end).map { trkpt ->
                        val zippedLocations =zipMapBoxLocations(trkpt).sortedBy { it.timestamp }
                        val polylineOptions = zippedLocations.zipWithNext().map { (start, end) ->
                            PolylineAnnotationOptions()
                                .withPoints(listOf(start.point, end.point))
                                .withLineColor(start.color.toColorInt())
                                .withLineWidth(5.0)
                        }

                        // On success, emit the Success state with the data.
                        MapHistoryUiState.Success(
                            trackPolyline = polylineOptions
                        )
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            // The initial state for the UI is always Loading.
            initialValue = MapHistoryUiState.Loading
        )
}