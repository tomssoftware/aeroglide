package de.tomssoftware.aeroglide.feature.dataexchange

import android.app.Application
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tomssoftware.aeroglide.core.ui.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ShareEvent {
    data class ShareFile(val intent: Intent) : ShareEvent()
    data class Error(val message: String) : ShareEvent()
}

@HiltViewModel
class DataExportViewModel @Inject constructor(
    private val application: Application,
    private val exporter: Exporter
) : ViewModel() {

    private val _shareEventChannel = Channel<ShareEvent>()
    val shareEventFlow = _shareEventChannel.receiveAsFlow()

    /**
     * The single entry point for the UI to request an export.
     * It now delegates all the hard work to the Exporter.
     */
    fun onExport(trackId: Long, format: String) {
        viewModelScope.launch {
            // 3. Call the Exporter to create the file.
            // The Exporter handles all data fetching, file creation, and zipping.
            val exportFile = exporter.createExportFile(trackId, format)

            if (exportFile != null && exportFile.exists()) {
                // 4. Create the ACTION_SEND intent with the file provided by the Exporter.
                val authority = "${application.packageName}"
                val uri = FileProvider.getUriForFile(application, authority, exportFile)

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = application.contentResolver.getType(uri)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooserIntent = Intent.createChooser(
                    shareIntent,
                    application.getString(R.string.sid_share) // Use a more specific string
                )

                // 5. Send the event to the UI to trigger the share sheet.
                _shareEventChannel.send(ShareEvent.ShareFile(chooserIntent))
            } else {
                // 6. Handle the case where the Exporter fails to create a file.
                _shareEventChannel.send(ShareEvent.Error("Failed to create export file."))
            }
        }
    }
}
