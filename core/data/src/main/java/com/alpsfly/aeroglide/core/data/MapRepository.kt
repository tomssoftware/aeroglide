package com.alpsfly.aeroglide.core.data

import android.content.Context
import com.alpsfly.aeroglide.core.model.mapsforge.Region
import com.alpsfly.aeroglide.core.model.mapsforge.RegionList
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

// Define an interface for testability
interface MapRepository {
    fun loadRegions(): List<Region>
    fun readMapFile(lat: Double, lon: Double): Flow<DownloadState>
}

// ✅ 1. REDESIGN DownloadState as a sealed interface
/**
 * Represents the distinct states of a file download operation.
 * This is unambiguous and type-safe.
 */
sealed interface DownloadState {
    /** The download is in progress. */
    data class Loading(val progress: Float) : DownloadState

    /** The download completed successfully. */
    data class Success(val file: File) : DownloadState

    /** An error occurred during the download. */
    data class Error(val message: String) : DownloadState
}

@Singleton
class MapRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : MapRepository {

    private val regions: List<Region> by lazy {
        loadRegionsFromAssets("regions.json")
    }

    override fun loadRegions(): List<Region> {
        return regions
    }

    private fun loadRegionsFromAssets(fileName: String): List<Region> {
        val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
        val gson = Gson()
        val regionList = gson.fromJson(json, RegionList::class.java)
        return regionList.regions
    }

    private fun readMapFile(region: Region?): File? {
        region?.let {
            val localFile = File(context.filesDir, it.relativePath)
            if (localFile.exists()) {
                Timber.i("Found downloaded map file at: ${localFile.path}")
                return localFile
            }
        }
        Timber.w("Specific map not found. Falling back to bundled world.map.")
        return copyAssetToCache("world.map")
    }

    private fun copyAssetToCache(fileName: String): File? {
        return try {
            val cacheFile = File(context.cacheDir, fileName)
            if (cacheFile.exists()) {
                Timber.d("Asset '$fileName' already in cache.")
                return cacheFile
            }
            Timber.d("Copying asset '$fileName' to cache...")
            context.assets.open(fileName).use { inputStream ->
                FileOutputStream(cacheFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            cacheFile
        } catch (e: Exception) {
            Timber.e(e, "Failed to copy asset '$fileName' to cache.")
            null
        }
    }

    override fun readMapFile(lat: Double, lon: Double): Flow<DownloadState> = flow {
        val region = loadRegions().firstOrNull { it.boundingBox.contains(lat, lon) }

        if (region == null) {
            val worldMap = readMapFile(null)
            if (worldMap != null) {
                emit(DownloadState.Success(worldMap)) // Emit Success
            } else {
                emit(DownloadState.Error("No region found and no fallback map available.")) // Emit Error
            }
            return@flow
        }

        val destinationFile = File(context.filesDir, region.relativePath)
        if (destinationFile.exists()) {
            Timber.i("Map for region '${region.name}' already exists.")
            emit(DownloadState.Success(destinationFile)) // Emit Success
            return@flow
        }

        Timber.i("Map for region '${region.name}' not found. Starting download.")
        emitAll(downloadMap(region)) // Delegate to the download function

    }.flowOn(Dispatchers.IO)

    private fun downloadMap(region: Region): Flow<DownloadState> = flow {
        try {
            emit(DownloadState.Loading(0f)) // Start with 0% progress

            val client = OkHttpClient()
            val request = Request.Builder().url(region.url).build()
            val response = client.newCall(request).execute()

            response.body.let { body ->
                val total = body.contentLength()
                if (total <= 0) {
                    emit(DownloadState.Error("Cannot determine file size."))
                    return@let
                }
                val outputFile = File(context.filesDir, region.relativePath)
                outputFile.parentFile?.mkdirs()

                var downloaded = 0L
                body.byteStream().use { input ->
                    FileOutputStream(outputFile).use { output ->
                        val buffer = ByteArray(1024 * 1024)
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            downloaded += read
                            // Emit Loading state with progress
                            emit(DownloadState.Loading(downloaded.toFloat() / total.toFloat()))
                        }
                    }
                }
                // Emit Success state with the final file
                emit(DownloadState.Success(outputFile))
            }
        } catch (e: Exception) {
            // Emit Error state on any exception
            emit(DownloadState.Error(e.message ?: "Unknown download error"))
        }
    }.flowOn(Dispatchers.IO)
}
