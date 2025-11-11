package com.alpsfly.aeroglide.core.domain.usecase

import com.alpsfly.aeroglide.core.domain.usecase.location.ServiceStarter
import timber.log.Timber
import javax.inject.Inject

/**
 * The "Manager" for the recording process.
 * This is a lightweight, stateless orchestrator.
 */
class RecordingUseCase @Inject constructor(
    private val recordingProcessor: RecordingProcessor, // The "worker buddy"
    private val serviceStarter: ServiceStarter      // The hardware manager
) {
    fun startRecording(activityId: Long) {
        Timber.i("RecordingUseCase: Commanding START")

        // 1. Tell the "worker buddy" to start processing data.
        recordingProcessor.start(activityId)

        // 2. Tell the "hardware manager" to keep the sensors awake.
        serviceStarter.startRecordingService()
    }

    fun stopRecording() {
        Timber.i("RecordingUseCase: Commanding STOP")

        // 1. Tell the worker to stop processing.
        recordingProcessor.stop()

        // 2. Tell the hardware manager to let the sensors sleep.
        serviceStarter.stopRecordingService()
    }
}
