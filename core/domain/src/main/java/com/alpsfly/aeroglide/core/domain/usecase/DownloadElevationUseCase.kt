package com.alpsfly.aeroglide.core.domain.usecase

import com.alpsfly.aeroglide.core.data.DownloadState
import com.alpsfly.aeroglide.core.data.ElevationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DownloadElevationUseCase @Inject constructor(
    private val elevationRepository: ElevationRepository
) {
    /**
     * Ensures the DEM tile for the given location is available, downloading it if necessary.
     */
    operator fun invoke(lat: Double, lon: Double): Flow<DownloadState> {
        return elevationRepository.getOrDownloadDemTileForLocation(lat, lon)
    }
}
