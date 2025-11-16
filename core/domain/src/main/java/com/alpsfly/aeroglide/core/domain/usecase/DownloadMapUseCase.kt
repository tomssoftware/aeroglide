package com.alpsfly.aeroglide.core.domain.usecase

import com.alpsfly.aeroglide.core.data.DownloadState
import com.alpsfly.aeroglide.core.data.MapRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * A lightweight "Manager" UseCase that provides on-demand map access.
 * It takes the current location and delegates the actual work to the MapRepository.
 */
class DownloadMapUseCase @Inject constructor(
    private val mapRepository: MapRepository
) {
    /**
     * The 'invoke' operator allows calling the UseCase like a function.
     * It immediately returns a cold Flow that, when collected, will either find
     * the local map file or download it.
     *
     * @param lat The current latitude.
     * @param lon The current longitude.
     * @return A Flow that emits the various states of the download process (Loading, Success, Error).
     */
    operator fun invoke(lat: Double, lon: Double): Flow<DownloadState> {
        // The UseCase's only job is to delegate the call to the repository.
        // It returns the Flow<DownloadState> that the repository creates.
        return mapRepository.readMapFile(lat, lon)
    }
}
