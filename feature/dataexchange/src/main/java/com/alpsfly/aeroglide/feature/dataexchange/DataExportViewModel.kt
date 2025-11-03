package com.alpsfly.aeroglide.feature.dataexchange

import android.app.Application
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.util.Csv
import com.alpsfly.aeroglide.core.data.util.Gpx
import com.alpsfly.aeroglide.core.data.util.Igc
import com.alpsfly.aeroglide.core.model.database.Location
import com.alpsfly.aeroglide.core.ui.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class ShareEvent {
    data class ShareFile(val intent: Intent) : ShareEvent()
}

@HiltViewModel
class DataExportViewModel @Inject constructor(
    private val application: Application,
    private val dataRepository: DataRepository,
) : ViewModel() {

    private val _shareEventChannel = Channel<ShareEvent>()
    val shareEventFlow = _shareEventChannel.receiveAsFlow() // The UI will collect this

    fun onExport(trackId: Long, format: String) {
        viewModelScope.launch {
            dataRepository.getActivity(trackId)?.let { it ->
                dataRepository.getLocationsBetween(it.begin, it.end).collect { locations ->
                    val content = when (format) {
                        "CSV" -> getCsv(locations)
                        "GPX" -> getGpx(locations)
                        "IGC" -> getIgc(locations, it.begin, "pilot", "gliderType", "gliderId")
                        else -> {
                            "Format not supported"
                        }
                    }

                    // 1. Create the file to be exported (this is just an example)
                    val exportFile = createExportFile(trackId, format, content)
                    if (exportFile != null) {
                        // 2. Create the ACTION_SEND intent
                        val authority = "${application.packageName}"
                        val uri = FileProvider.getUriForFile(application, authority, exportFile)

                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = application.contentResolver.getType(uri)
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        val chooserIntent = Intent.createChooser(
                            shareIntent,
                            application.getString(R.string.sid_settings)
                        )

                        // 3. Send the event to the UI to trigger the share sheet
                        _shareEventChannel.send(ShareEvent.ShareFile(chooserIntent))
                    }
                }
            }
        }
    }

    private fun createExportFile(trackId: Long, format: String, content: String): File? {
        val exportDir = File(application.cacheDir, "exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        val file = File(exportDir, "track_${trackId}.$format")
        file.writeText(content)
        return file
    }

    fun getGpx(locations: List<Location>): String {
        return Gpx(locations).buildPath()
    }

    fun getCsv(trackLog: List<Location>): String {
        return Csv(trackLog).buildCsv()
    }

    fun getIgc(
        trackLog: List<Location>,
        departureDate: Long,
        pilot: String,
        gliderType: String,
        gliderId: String
    ): String {
        return Igc(trackLog, departureDate, pilot, gliderType, gliderId).buildIgc()
    }
}
