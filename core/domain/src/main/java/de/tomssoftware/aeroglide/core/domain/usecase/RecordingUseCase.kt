package de.tomssoftware.aeroglide.core.domain.usecase

import de.tomssoftware.aeroglide.core.data.util.SyncManager
import de.tomssoftware.aeroglide.core.domain.usecase.location.LocationServiceStarter
import timber.log.Timber
import javax.inject.Inject

/**
 * The "Manager" for the recording process.
 * This is a lightweight, stateless orchestrator.
 */
class RecordingUseCase @Inject constructor(
    private val recordingProcessor: RecordingProcessor, // The "worker buddy"
    private val locationServiceStarter: LocationServiceStarter,  // The hardware manager
    private val syncManager: SyncManager
) {
    fun startRecording(activityId: Long) {
        Timber.i("RecordingUseCase: Commanding START")

        // 1. Tell the "worker buddy" to start processing data.
        recordingProcessor.start(activityId)

        // 2. Tell the "hardware manager" to keep the sensors awake.
        locationServiceStarter.startForegroundService()
    }

    fun stopRecording() {
        Timber.i("RecordingUseCase: Commanding STOP")

        // 1. Tell the worker to stop processing.
        recordingProcessor.stop()

        // 2. Tell the hardware manager to let the sensors sleep.
        locationServiceStarter.stopForegroundService()

        // 3. Tell the sync manager to sync.
        syncManager.requestSync()
    }
}
