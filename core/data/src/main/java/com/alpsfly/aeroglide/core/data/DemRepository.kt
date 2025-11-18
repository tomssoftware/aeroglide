package com.alpsfly.aeroglide.core.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.floor

// Using the same sealed interface for download state is perfect.
// No new classes needed here.

interface DemRepository {
    /**
     * The core function. Takes a lat/lon, determines the required tile,
     * checks if it's downloaded, and if not, downloads it.
     * Returns a flow that emits the download state.
     */
    fun getOrDownloadDemTileForLocation(lat: Double, lon: Double, quality: DemQuality = DemQuality.HIGH): Flow<DownloadState>

    /**
     * Gets a tile file if it exists locally, without attempting to download.
     * Returns null if not found.
     */
    fun getLocalDemTile(lat: Double, lon: Double, quality: DemQuality = DemQuality.HIGH): File?
}

enum class DemQuality(val path: String) {
    STANDARD("dem3"), // 3-arc-second
    HIGH("dem1")      // 1-arc-second
}

sealed interface DownloadState {
    /** The download is in progress. */
    data class Loading(val progress: Float) : DownloadState

    /** The download completed successfully. */
    data class Success(val file: File) : DownloadState

    /** An error occurred during the download. */
    data class Error(val message: String) : DownloadState
}

@Singleton
class DemRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : DemRepository {

    companion object {
        // Base URL for the high-resolution DEM data source you found.
        private const val DEM1_BASE_URL = "https://dwtkns.com/srtm30m"
        private const val DEM3_BASE_URL = "https://download.mapsforge.org/maps/dem/dem3"
    }

    override fun getOrDownloadDemTileForLocation(lat: Double, lon: Double, quality: DemQuality): Flow<DownloadState> = flow {
        // 1. Calculate the required tile name from the coordinates.
        val tileName = getTileNameForLocation(lat, lon)
        if (tileName == null) {
            emit(DownloadState.Error("Invalid coordinates."))
            return@flow
        }

        // 2. Check if the unzipped .hgt file already exists locally.
        val destinationDir = File(context.filesDir, "dem/${quality.path}/")
        val hgtFile = File(destinationDir, "$tileName.hgt")

        if (hgtFile.exists()) {
            Timber.d("DEM tile '$tileName.hgt' already exists locally.")
            emit(DownloadState.Success(hgtFile))
            return@flow
        }

        // 3. If not, proceed with the download.
        // Example: 48.5 -> N40, -12.5 -> S10
        val subFolder = getSubfolderForLocation(lat)
        val url = "$DEM3_BASE_URL/$subFolder/$tileName.hgt.zip"
        val zipFile = File(destinationDir, "$tileName.hgt.zip")
        Timber.i("DEM tile '$tileName.hgt' not found. Starting download from '$url' to '$zipFile'.")

        try {
            emit(DownloadState.Loading(0f))

            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                throw Exception("Download failed with code: ${response.code}")
            }

            response.body.let { body ->
                val total = body.contentLength()
                if (total <= 0) {
                    emit(DownloadState.Error("Cannot determine file size."))
                    return@let
                }
                zipFile.parentFile?.mkdirs()

                var downloaded = 0L
                body.byteStream().use { input ->
                    FileOutputStream(zipFile).use { output ->
                        val buffer = ByteArray(1024 * 1024) // 1MB buffer
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            downloaded += read
                            emit(DownloadState.Loading(downloaded.toFloat() / total.toFloat()))
                        }
                    }
                }

                // 4. Unzip the file.
                Timber.d("Unzipping '$zipFile' to '$hgtFile'")
                unzip(zipFile, hgtFile)
                zipFile.delete() // Clean up the downloaded zip file

                // 5. Emit final success state with the path to the .hgt file.
                emit(DownloadState.Success(hgtFile))
            }
        } catch (e: Exception) {
            emit(DownloadState.Error(e.message ?: "Unknown download error"))
            zipFile.delete() // Clean up failed download
        }
    }.flowOn(Dispatchers.IO)


    override fun getLocalDemTile(lat: Double, lon: Double, quality: DemQuality): File? {
        val tileName = getTileNameForLocation(lat, lon) ?: return null
        val hgtFile = File(context.filesDir, "dem/${quality.path}/$tileName.hgt")
        return if (hgtFile.exists()) hgtFile else null
    }

    /**
     * Calculates the SRTM tile name (e.g., "N47E009") for a given lat/lon.
     */
    private fun getTileNameForLocation(lat: Double, lon: Double): String? {
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) return null

        val latHemisphere = if (lat >= 0) 'N' else 'S'
        val lonHemisphere = if (lon >= 0) 'E' else 'W'

        val latInt = floor(lat).toInt()
        val lonInt = floor(lon).toInt()

        return String.format("%c%02d%c%03d", latHemisphere, kotlin.math.abs(latInt), lonHemisphere, kotlin.math.abs(lonInt))
    }

    /**
     * Calculates the 10-degree band subfolder name (e.g., "N40", "S10").
     */
    private fun getSubfolderForLocation(lat: Double): String {
        val latHemisphere = if (lat >= 0) "N" else "S"

        // Round down to nearest 10 (e.g., 47 -> 40, 12 -> 10, 5 -> 00)
        val latTens = (floor(abs(lat)).toInt() / 1) * 1

        return String.format("%s%02d", latHemisphere, latTens)
    }

    /**
     * Unzips a single-entry zip file.
     */
    private fun unzip(zipFile: File, targetFile: File) {
        ZipInputStream(zipFile.inputStream()).use { zis ->
            zis.nextEntry // Move to the first and only entry
            FileOutputStream(targetFile).use { fos ->
                zis.copyTo(fos)
            }
        }
    }
}
