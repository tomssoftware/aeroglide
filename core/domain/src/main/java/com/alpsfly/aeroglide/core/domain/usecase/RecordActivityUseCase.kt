package com.alpsfly.aeroglide.core.domain.usecase

import android.content.Context
import android.os.PowerManager
import androidx.core.content.ContextCompat.getSystemService
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.domain.usecase.RecordActivityUseCase.VarioAccuracy
import com.alpsfly.aeroglide.core.model.database.Activity
import com.alpsfly.aeroglide.core.model.database.Altitude
import com.alpsfly.aeroglide.core.model.database.Climbrate
import com.alpsfly.aeroglide.core.model.database.Location
import com.alpsfly.aeroglide.core.model.database.Pressure
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Duration
import javax.inject.Inject
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

class RecordActivityUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val sensorRepository: SensorRepository,
    private val dataRepository: DataRepository,
) {

    enum class RecordingSensorType {
        ALTITUDE, CLIMBRATE, PRESSURE, LOCATION
    }

    private val recordingJobs = mutableMapOf<RecordingSensorType, Job>()

    private val sensorFlows = listOf(
        RecordingSensorType.ALTITUDE to sensorRepository.altitudeFlowUi,
        RecordingSensorType.CLIMBRATE to sensorRepository.climbrateFlowUi,
        RecordingSensorType.PRESSURE to sensorRepository.pressureFlowUi,
        RecordingSensorType.LOCATION to sensorRepository.locationFlowUi,
    )

    enum class VarioAccuracy {
        NONE, // Initial or unknown or no GPS
        LOW,  // Acceleration
        MID,  // Acceleration and Location
        HIGH  // Acceleration and Location and Pressure
    }

    private var recordingWakeLock: PowerManager.WakeLock? = null
    private var recordingActivity = Activity()
    private val verticallyMoving = VerticallyMoving() // todo: reset after activity end

    fun startRecording(activityId: Long) {
        check(activityId != 0L)
        Timber.d("startRecording")
        CoroutineScope(Dispatchers.IO).launch {
            recordingActivity = insertActivity(activityId)
        }
        startRecordSensorData()
    }

    fun stopRecording() {
        Timber.d("stopRecording")
        stopRecordSensorData()
    }

    private fun startRecordSensorData() {
        recordingWakeLock = (getSystemService(context, PowerManager::class.java) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TrackRecorder::lock").apply {
                acquire(Duration.ofHours(12).toMillis())
            }
        }

        sensorRepository.enableSensorListener()
        sensorFlows.forEach { (type, flow) ->
            val job = CoroutineScope(Dispatchers.IO).launch {
                verticallyMoving.reset()

                flow.collect { data ->
                    when (type) {
                        RecordingSensorType.ALTITUDE -> {
                            dataRepository.addAltitude(data as Altitude)
                            if (data.altitude > recordingActivity.maxAltitude) {
                                recordingActivity.maxAltitude = data.altitude
                                updateActivity(recordingActivity)
                            }
                            if (data.altitude < recordingActivity.minAltitude) {
                                recordingActivity.minAltitude = data.altitude
                                updateActivity(recordingActivity)
                            }

                            verticallyMoving.update(data.altitude)
                            recordingActivity.ascent = verticallyMoving.getAscent()
                            recordingActivity.descent = verticallyMoving.getDescent()

                            updateActivity(recordingActivity)
                        }

                        RecordingSensorType.CLIMBRATE -> {
                            dataRepository.addClimbrate(data as Climbrate)
                            if (data.climbrate > recordingActivity.maxClimbrate) {
                                recordingActivity.maxClimbrate = data.climbrate
                                updateActivity(recordingActivity)
                            }
                            if (data.climbrate < recordingActivity.minClimbrate) {
                                recordingActivity.minClimbrate = data.climbrate
                                updateActivity(recordingActivity)
                            }
                        }

                        RecordingSensorType.PRESSURE -> {
                            dataRepository.addPressure(data as Pressure)
                            if (data.pressure > recordingActivity.maxPressure) {
                                recordingActivity.maxPressure = data.pressure
                                updateActivity(recordingActivity)
                            }
                            if (data.pressure < recordingActivity.minPressure) {
                                recordingActivity.minPressure = data.pressure
                                updateActivity(recordingActivity)
                            }
                        }

                        RecordingSensorType.LOCATION -> {
                            dataRepository.addLocation(data as Location)
                            recordingActivity.distance += distance(data)
                            recordingActivity.duration = (recordingActivity.end - recordingActivity.begin) / 1000
                            recordingActivity.maxSpeed = max(data.speed, recordingActivity.maxSpeed)
                            recordingActivity.minSpeed = min(data.speed, recordingActivity.minSpeed)
                            if (recordingActivity.duration > 0) {
                                recordingActivity.avgSpeed = (recordingActivity.distance / recordingActivity.duration)
                                recordingActivity.positiveAvgClimbrate =
                                    (recordingActivity.ascent / recordingActivity.duration)
                                recordingActivity.negativeAvgClimbrate =
                                    (recordingActivity.descent / recordingActivity.duration)
                            }
                            recordingActivity.end = System.currentTimeMillis()
                            updateActivity(recordingActivity)
                        }
                    }
                }
            }
            recordingJobs[type] = job
        }
    }

    private fun stopRecordSensorData() {
        recordingWakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }

        recordingJobs.forEach { (_, job) -> job.cancel() }
        recordingJobs.clear()
        sensorRepository.disableSensorListener()
    }

    private suspend fun insertActivity(activityId: Long): Activity {
        val now = System.currentTimeMillis()
        val activity = Activity(
            activityId = activityId,
            userId = "thomas.fiedler@fn.de",
            begin = now,
            end = now,
            distance = 0f
        )
        dataRepository.addActivity(activity)
        return activity
    }

    private suspend fun updateActivity(activity: Activity) {
        dataRepository.updateActivity(activity)
    }

    private var lastLocation = Location()
    private fun distance(currentLocation: Location): Float {
        if (lastLocation.timestamp == 0L) {
            lastLocation = currentLocation
            return 0f
        }
        val locC = android.location.Location("none").apply {
            latitude = currentLocation.latitude.toDouble()
            longitude = currentLocation.longitude.toDouble()
        }
        val locP = android.location.Location("none").apply {
            latitude = lastLocation.latitude.toDouble()
            longitude = lastLocation.longitude.toDouble()
        }
        lastLocation = currentLocation
        return locC.distanceTo(locP)
    }
}

class VerticallyMoving(
    private var ascent: Float = 0f,
    private var descent: Float = 0f,
    private var delta: Float = 0f,
    private var lastAltitude: Float = 0f
) {
    fun update(altitude: Float) {
        if ((altitude - lastAltitude).absoluteValue <= INVALID_ALTITUDE_DELTA) {
            delta += altitude - lastAltitude

            if (delta >= getAscentThreshold(VarioAccuracy.HIGH)) { // meter
                ascent += delta
                delta = 0f
            }
            if (delta <= getDescentThreshold(VarioAccuracy.HIGH)) { // meter
                descent += delta.absoluteValue
                delta = 0f
            }
        }
        lastAltitude = altitude
    }

    fun getAscent(): Float {
        return ascent
    }

    fun getDescent(): Float {
        return descent
    }

    fun reset() {
        ascent = 0f
        descent = 0f
        delta = 0f
        lastAltitude = 0f
    }

    private fun getAscentThreshold(accuracy: VarioAccuracy): Float = when (accuracy) {
        VarioAccuracy.HIGH -> ASCENT_THRESHOLD_HIGH_ACCURACY
        else -> ASCENT_THRESHOLD_MID_ACCURACY
    }

    private fun getDescentThreshold(accuracy: VarioAccuracy): Float = when (accuracy) {
        VarioAccuracy.HIGH -> DESCENT_THRESHOLD_HIGH_ACCURACY
        else -> DESCENT_THRESHOLD_MID_ACCURACY
    }

    companion object {
        private const val INVALID_ALTITUDE_DELTA = 25f
        private const val ASCENT_THRESHOLD_HIGH_ACCURACY = 1f
        private const val DESCENT_THRESHOLD_HIGH_ACCURACY = -1f

        private const val ASCENT_THRESHOLD_MID_ACCURACY = 10f
        private const val DESCENT_THRESHOLD_MID_ACCURACY = -10f
    }

}

