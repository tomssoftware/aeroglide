package com.alpsfly.aeroglide.core.domain.usecase

import com.alpsfly.aeroglide.core.data.DemRepository
import com.alpsfly.aeroglide.core.data.DownloadState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DownloadDemUseCase @Inject constructor(
    private val demRepository: DemRepository
) {
    /**
     * Ensures the DEM tile for the given location is available, downloading it if necessary.
     */
    operator fun invoke(lat: Double, lon: Double): Flow<DownloadState> {
        return demRepository.getOrDownloadDemTileForLocation(lat, lon)
    }
}
