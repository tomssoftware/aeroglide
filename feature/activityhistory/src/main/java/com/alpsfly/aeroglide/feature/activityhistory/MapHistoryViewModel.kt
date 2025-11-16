package com.alpsfly.aeroglide.feature.activityhistory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.DownloadState
import com.alpsfly.aeroglide.core.domain.usecase.DownloadMapUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.mapsforge.core.model.Point
import java.io.File
import javax.inject.Inject

// The new, robust UiState definition
sealed interface MapHistoryUiState {
    data object Loading : MapHistoryUiState
    data class Downloading(val progress: Float) : MapHistoryUiState
    data class Success(val mapFile: File, val trackPoints: List<Point>, val startPosition: Point?) : MapHistoryUiState
    data class Error(val message: String) : MapHistoryUiState
}

@HiltViewModel
class MapHistoryViewModel @Inject constructor(
    private val dataRepository: DataRepository,
    private val downloadMapUseCase: DownloadMapUseCase, // Inject the use case
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<MapHistoryUiState>(MapHistoryUiState.Loading)
    val uiState: StateFlow<MapHistoryUiState> = _uiState.asStateFlow()

    init {
        val activityId: Long? = savedStateHandle["activityId"]
        if (activityId != null && activityId != 0L) {
            loadMapAndTrack(activityId)
        } else {
            _uiState.value = MapHistoryUiState.Error("Invalid Activity ID provided.")
        }
    }

    private fun loadMapAndTrack(activityId: Long) {
        viewModelScope.launch {
            _uiState.value = MapHistoryUiState.Loading

            // 1. Fetch the activity and track points first.
            val activity = dataRepository.getActivity(activityId)
            if (activity == null) {
                _uiState.value = MapHistoryUiState.Error("Activity with ID $activityId not found.")
                return@launch
            }

            val locations = dataRepository.getLocationsBetween(activity.begin, activity.end).first()
            val trackPoints = locations.map { Point(it.longitude.toDouble(), it.latitude.toDouble()) }

            if (trackPoints.isEmpty()) {
                _uiState.value = MapHistoryUiState.Error("This activity has no track data to display.")
                return@launch
            }

            val startPosition = trackPoints.first()
            val startLat = startPosition.y
            val startLon = startPosition.x

            // 2. Now, call the DownloadMapUseCase to get the map file.
            //    This will either return the file immediately or start a download.
            downloadMapUseCase(startLat, startLon).collect { downloadState ->
                // 3. Update the UiState based on the emissions from the DownloadState flow.
                when (downloadState) {
                    is DownloadState.Loading -> {
                        _uiState.value = MapHistoryUiState.Downloading(downloadState.progress)
                    }

                    is DownloadState.Success -> {
                        _uiState.value = MapHistoryUiState.Success(
                            mapFile = downloadState.file,
                            trackPoints = trackPoints,
                            startPosition = startPosition
                        )
                    }

                    is DownloadState.Error -> {
                        _uiState.value = MapHistoryUiState.Error(downloadState.message)
                    }
                }
            }
        }
    }
}
