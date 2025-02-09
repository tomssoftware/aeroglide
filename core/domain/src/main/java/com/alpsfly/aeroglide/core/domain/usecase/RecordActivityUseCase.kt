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
import java.time.Duration
import javax.inject.Inject
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

class RecordActivityUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
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

    private var activity = Activity()
    private val verticallyMoving = VerticallyMoving() // todo: reset after activity end

    fun startRecording(activityId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            activity = insertActivity(activityId)
        }
        startRecordSensorData()
    }

    fun stopRecording() {
        stopRecordSensorData()
    }

    private fun startRecordSensorData() {
        recordingWakeLock = (getSystemService(context, PowerManager::class.java) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TrackRecorder::lock").apply {
                acquire(Duration.ofHours(12).toMillis())
            }
        }

        sensorRepository.enableListener()
        sensorFlows.forEach { (type, flow) ->
            val job = CoroutineScope(Dispatchers.IO).launch {
                verticallyMoving.reset()

                flow.collect { data ->
                    when (type) {
                        RecordingSensorType.ALTITUDE -> {
                            dataRepository.addAltitude(data as Altitude)
                            if (data.altitude > activity.maxAltitude) {
                                activity.maxAltitude = data.altitude
                                updateActivity(activity)
                            }
                            if (data.altitude < activity.minAltitude) {
                                activity.minAltitude = data.altitude
                                updateActivity(activity)
                            }

                            verticallyMoving.update(data.altitude)
                            activity.ascent = verticallyMoving.getAscent()
                            activity.descent = verticallyMoving.getDescent()

                            updateActivity(activity)
                        }

                        RecordingSensorType.CLIMBRATE -> {
                            dataRepository.addClimbrate(data as Climbrate)
                            if (data.climbrate > activity.maxClimbrate) {
                                activity.maxClimbrate = data.climbrate
                                updateActivity(activity)
                            }
                            if (data.climbrate < activity.minClimbrate) {
                                activity.minClimbrate = data.climbrate
                                updateActivity(activity)
                            }
                        }

                        RecordingSensorType.PRESSURE -> {
                            dataRepository.addPressure(data as Pressure)
                            if (data.pressure > activity.maxPressure) {
                                activity.maxPressure = data.pressure
                                updateActivity(activity)
                            }
                            if (data.pressure < activity.minPressure) {
                                activity.minPressure = data.pressure
                                updateActivity(activity)
                            }
                        }

                        RecordingSensorType.LOCATION -> {
                            dataRepository.addLocation(data as Location)
                            activity.distance += distance(data)
                            activity.duration = (activity.end - activity.begin) / 1000
                            activity.maxSpeed = max(data.speed, activity.maxSpeed)
                            activity.minSpeed = min(data.speed, activity.minSpeed)
                            if (activity.duration > 0) {
                                activity.avgSpeed = (activity.distance / activity.duration)
                                activity.positiveAvgClimbrate = (activity.ascent / activity.duration)
                                activity.negativeAvgClimbrate = (activity.descent / activity.duration)
                            }
                            activity.end = System.currentTimeMillis()
                            updateActivity(activity)
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
        sensorRepository.disableListener()
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

