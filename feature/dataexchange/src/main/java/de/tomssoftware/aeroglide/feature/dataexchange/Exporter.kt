package de.tomssoftware.aeroglide.feature.dataexchange

import android.content.Context
import de.tomssoftware.aeroglide.core.data.DataRepository
import de.tomssoftware.aeroglide.core.data.util.CsvExporter
import de.tomssoftware.aeroglide.core.data.util.Gpx
import de.tomssoftware.aeroglide.core.data.util.Igc
import de.tomssoftware.aeroglide.core.model.database.Activity
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
            "GPX" -> createSingleFile(activity, exportDir, "gpx") { trackPoints ->
                Gpx(trackPoints).buildPath()
            }

            "IGC" -> createSingleFile(activity, exportDir, "igc") { trackPoints ->
                Igc(trackPoints, activity.begin, "Pilot", "Glider", "GliderID").buildIgc()
            }

            else -> null
        }
    }

    /**
     * Creates a ZIP archive containing multiple CSV files for a given activity.
     */
    private suspend fun createZipArchive(activity: Activity, exportDir: File): File {
        val zipFile = File(exportDir, "track_${activity.activityId}.zip")
        val trackPoints = dataRepository.getTracksBetween(activity.begin, activity.end).first()


        // Create a list of temporary files to be zipped
        val filesToZip = mutableListOf<File>()
        filesToZip.add(createTempFile(exportDir, "track_points.csv", CsvExporter.buildCsv(trackPoints)))

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
        contentBuilder: suspend (List<de.tomssoftware.aeroglide.core.model.database.TrackPoint>) -> String
    ): File {
        val file = File(exportDir, "track_${activity.activityId}.$extension")
        val trackPoints = dataRepository.getTracksBetween(activity.begin, activity.end).first()
        file.writeText(contentBuilder(trackPoints))
        return file
    }

    /**
     * Helper to write content to a temporary file.
     */
    private fun createTempFile(dir: File, name: String, content: String): File {
        return File(dir, name).apply { writeText(content) }
    }
}
