package com.alpsfly.aeroglide.feature.mapmanager

import androidx.lifecycle.ViewModel
import com.alpsfly.aeroglide.core.data.MapRepository
import com.alpsfly.aeroglide.core.model.mapsforge.Region
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class MapManagerViewModel @Inject constructor(
    mapRepository: MapRepository
) : ViewModel() {
    // Expose regions as state
    private val _regions = MutableStateFlow<List<Region>>(emptyList())

    init {
        // Load regions once on initialization
        _regions.value = mapRepository.loadRegions()
    }
}
