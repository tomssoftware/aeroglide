package com.alpsfly.aeroglide.feature.dataexchange

import android.content.Context
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.util.CsvExporter
import com.alpsfly.aeroglide.core.data.util.Gpx
import com.alpsfly.aeroglide.core.data.util.Igc
import com.alpsfly.aeroglide.core.model.database.Activity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject

/**
 * A dedicated "worker" class responsible for handling all complex data export logic.
 * It fetches data, creates files (CSV, GPX, IGC), and zips them.
 */
class Exporter @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val dataRepository: DataRepository
) {
    /**
     * The main public function. Orchestrates the creation of an export file.
     * @return The final export file (e.g., track_123.zip, track_123.igc) or null on failure.
     */
    suspend fun createExportFile(trackId: Long, format: String): File? {
        val activity = dataRepository.getActivity(trackId) ?: return null
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }

        return when (format.uppercase()) {
            "ZIP" -> createZipArchive(activity, exportDir)
            "GPX" -> createSingleFile(activity, exportDir, "gpx") { locations ->
                Gpx(locations).buildPath()
            }

            "IGC" -> createSingleFile(activity, exportDir, "igc") { locations ->
                Igc(locations, activity.begin, "Pilot", "Glider", "GliderID").buildIgc()
            }

            else -> null
        }
    }

    /**
     * Creates a ZIP archive containing multiple CSV files for a given activity.
     */
    private suspend fun createZipArchive(activity: Activity, exportDir: File): File {
        val zipFile = File(exportDir, "track_${activity.activityId}.zip")
        val locations = dataRepository.getLocationsBetween(activity.begin, activity.end).first()
        val altitudes = dataRepository.getAltitudesBetween(activity.begin, activity.end).first()
        val climbrates = dataRepository.getClimbratesBetween(activity.begin, activity.end).first()
        val pressures = dataRepository.getPressuresBetween(activity.begin, activity.end).first()
        val glideRatios = dataRepository.getGlideRatiosBetween(activity.begin, activity.end).first()

        // Create a list of temporary files to be zipped
        val filesToZip = mutableListOf<File>()
        filesToZip.add(createTempFile(exportDir, "location.csv", CsvExporter.buildCsv(locations)))
        filesToZip.add(createTempFile(exportDir, "altitude.csv", CsvExporter.buildCsv(altitudes)))
        filesToZip.add(createTempFile(exportDir, "climbrate.csv", CsvExporter.buildCsv(climbrates)))
        filesToZip.add(createTempFile(exportDir, "pressure.csv", CsvExporter.buildCsv(pressures)))
        filesToZip.add(createTempFile(exportDir, "glide_ratio.csv", CsvExporter.buildCsv(glideRatios)))

        // Create the ZIP file
        FileOutputStream(zipFile).use { fos ->
            ZipOutputStream(fos).use { zos ->
                filesToZip.forEach { file ->
                    zos.putNextEntry(java.util.zip.ZipEntry(file.name))
                    file.inputStream().use { input -> input.copyTo(zos) }
                    zos.closeEntry()
                }
            }
        }

        // Clean up the temporary CSV files
        filesToZip.forEach { it.delete() }

        return zipFile
    }

    /**
     * Creates a single text-based file (like GPX or IGC).
     */
    private suspend fun createSingleFile(
        activity: Activity,
        exportDir: File,
        extension: String,
        contentBuilder: suspend (List<com.alpsfly.aeroglide.core.model.database.Location>) -> String
    ): File {
        val file = File(exportDir, "track_${activity.activityId}.$extension")
        val locations = dataRepository.getLocationsBetween(activity.begin, activity.end).first()
        file.writeText(contentBuilder(locations))
        return file
    }

    /**
     * Helper to write content to a temporary file.
     */
    private fun createTempFile(dir: File, name: String, content: String): File {
        return File(dir, name).apply { writeText(content) }
    }
}
