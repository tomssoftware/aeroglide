package com.alpsfly.aeroglide.core.domain.usecase

import android.content.Context
import android.os.PowerManager
import androidx.core.content.ContextCompat
import com.alpsfly.aeroglide.core.common.di.ApplicationScope
import com.alpsfly.aeroglide.core.data.AppRepository
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.model.database.Activity
import com.alpsfly.aeroglide.core.model.database.Altitude
import com.alpsfly.aeroglide.core.model.database.Climbrate
import com.alpsfly.aeroglide.core.model.database.Location
import com.alpsfly.aeroglide.core.model.database.Pressure
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

/**
 * The "Worker" for the recording process.
 * This is a singleton that contains all the business logic for collecting,
 * processing, and saving flight data. It runs in a background-safe scope.
 */
@Singleton
class RecordingProcessor @Inject constructor(
    private val appRepository: AppRepository,
    private val sensorRepository: SensorRepository,
    private val dataRepository: DataRepository,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
    @param:ApplicationContext private val context: Context
) {
    private val recordingJobs = mutableListOf<Job>()
    private var sessionActivity: Activity? = null
    private val verticallyMoving = VerticallyMoving()
    private var lastLocation: Location? = null
    private var recordingWakeLock: PowerManager.WakeLock? = null

    fun start(activityId: Long) {
        if (recordingJobs.isNotEmpty()) {
            Timber.w("RecordingProcessor is already running.")
            return
        }
        Timber.i("RecordingProcessor: Starting")

        startWakeLock()

        // We now do everything inside one managing coroutine.
        appRepository.setActivityId(activityId)
        applicationScope.launch {
            // 1. CREATE the new activity record here.
            insertActivity(activityId)

            // 2. Fetch the newly created activity to work with.
            sessionActivity = dataRepository.getActivity(activityId)
            Timber.i("Session activity created and fetched successfully for ID: $activityId")

            // 3. Reset state and start collecting.
            verticallyMoving.reset()
            lastLocation = null
            collectSensors()
        }
    }

    fun stop() {
        if (recordingJobs.isEmpty()) return
        Timber.i("RecordingProcessor: Stopping.")

        recordingJobs.forEach { it.cancel() }
        recordingJobs.clear()

        releaseWakeLock()
        sessionActivity = null
    }

    private fun collectSensors() {
        sensorRepository.altitudeFlowUi.onEach {
            processAltitude(it)
        }.launchIn(applicationScope)
            .also { recordingJobs.add(it) }
        sensorRepository.climbrateFlowUi.onEach {
            processClimbrate(it)
        }.launchIn(applicationScope)
            .also { recordingJobs.add(it) }
        sensorRepository.pressureFlowUi.onEach {
            processPressure(it)
        }.launchIn(applicationScope)
            .also { recordingJobs.add(it) }
        sensorRepository.locationFlowUi.onEach {
            processLocation(it)
        }.launchIn(applicationScope)
            .also { recordingJobs.add(it) }
    }

    private suspend fun persist() {
        sessionActivity?.let { dataRepository.updateActivity(it) }
    }

    // --- All data processing logic is now correctly placed here ---

    private suspend fun processAltitude(altitude: Altitude) {
        sessionActivity?.let { activity ->
            dataRepository.addAltitude(altitude)
            activity.maxAltitude = max(altitude.altitude, activity.maxAltitude)
            activity.minAltitude = min(altitude.altitude, activity.minAltitude)
            verticallyMoving.update(altitude.altitude)
            activity.ascent = verticallyMoving.getAscent()
            activity.descent = verticallyMoving.getDescent()
            persist()
        }
    }

    // ... (processClimbrate, processPressure, processLocation, computeDistance, WakeLock methods are all correct) ...
    private suspend fun processClimbrate(climbrate: Climbrate) {
        sessionActivity?.let { activity ->
            dataRepository.addClimbrate(climbrate)
            activity.maxClimbrate = max(climbrate.climbrate, activity.maxClimbrate)
            activity.minClimbrate = min(climbrate.climbrate, activity.minClimbrate)
            persist()
        }
    }

    private suspend fun processPressure(pressure: Pressure) {
        sessionActivity?.let { activity ->
            dataRepository.addPressure(pressure)
            activity.maxPressure = max(pressure.pressure, activity.maxPressure)
            activity.minPressure = min(pressure.pressure, activity.minPressure)
            persist()
        }
    }

    private suspend fun processLocation(loc: Location) {
        sessionActivity?.let { activity ->
            dataRepository.addLocation(loc)
            val distanceDelta = computeDistance(lastLocation, loc)
            activity.distance += distanceDelta
            activity.end = System.currentTimeMillis()
            val duration = (activity.end - activity.begin) / 1000
            activity.duration = duration
            activity.maxSpeed = max(loc.speed, activity.maxSpeed)
            activity.minSpeed = min(loc.speed, activity.minSpeed)
            if (duration > 0) {
                activity.avgSpeed = activity.distance / duration
                activity.positiveAvgClimbrate = activity.ascent / duration
                activity.negativeAvgClimbrate = activity.descent / duration
            }
            lastLocation = loc
            persist()
        }
    }

    private fun computeDistance(prev: Location?, current: Location): Float {
        if (prev?.timestamp == 0L || prev == null) return 0f
        val locC = android.location.Location("c").apply {
            latitude = current.latitude.toDouble()
            longitude = current.longitude.toDouble()
        }
        val locP = android.location.Location("p").apply {
            latitude = prev.latitude.toDouble()
            longitude = prev.longitude.toDouble()
        }
        return locP.distanceTo(locC)
    }

    private suspend fun insertActivity(id: Long) {
        val now = System.currentTimeMillis()
        val activity = Activity(
            activityId = id,
            userId = "user@example.com", // This should be dynamic
            begin = now,
            end = now,
            distance = 0f
        )
        dataRepository.addActivity(activity)
    }

    @OptIn(ExperimentalTime::class)
    private fun startWakeLock() {
        if (recordingWakeLock?.isHeld == true) return
        recordingWakeLock =
            (ContextCompat.getSystemService(
                context,
                PowerManager::class.java
            ) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AeroGlide::RecordingWakelock").apply {
                    acquire(12.hours.inWholeMilliseconds)
                }
            }
    }

    private fun releaseWakeLock() {
        recordingWakeLock?.let {
            if (it.isHeld) {
                it.release()
                recordingWakeLock = null
            }
        }
    }
}

// ✅ ADDED THE MISSING HELPER CLASS DEFINITION
class VerticallyMoving {
    private var lastAltitude: Float = 0f
    private var ascent: Float = 0f
    private var descent: Float = 0f

    fun update(altitude: Float) {
        if (lastAltitude != 0f) {
            val diff = altitude - lastAltitude
            if (diff > 0) {
                ascent += diff
            } else {
                descent += diff.absoluteValue
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
        lastAltitude = 0f
        ascent = 0f
        descent = 0f
    }
}
