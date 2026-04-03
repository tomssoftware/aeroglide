package de.tomssoftware.aeroglide.core.domain.usecase

import android.content.Context
import android.os.PowerManager
import androidx.core.content.ContextCompat
import de.tomssoftware.aeroglide.core.common.di.ApplicationScope
import de.tomssoftware.aeroglide.core.data.AppRepository
import de.tomssoftware.aeroglide.core.data.AuthRepository
import de.tomssoftware.aeroglide.core.data.DataRepository
import de.tomssoftware.aeroglide.core.data.SensorRepository
import de.tomssoftware.aeroglide.core.model.database.Activity
import de.tomssoftware.aeroglide.core.model.database.TrackPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
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
    private val authRepository: AuthRepository,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
    @param:ApplicationContext private val context: Context
) {
    private val recordingJobs = mutableListOf<Job>()
    private var sessionActivity: Activity? = null
    private val verticallyMoving = VerticallyMoving()
    private var lastLocation: TrackPoint? = null
    private var recordingWakeLock: PowerManager.WakeLock? = null

    fun start(activityId: Long) {
        if (recordingJobs.isNotEmpty()) {
            Timber.w("RecordingProcessor is already running.")
            return
        }
        Timber.d("RecordingProcessor: Starting")

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
        Timber.d("RecordingProcessor: Stopping.")

        recordingJobs.forEach { it.cancel() }
        recordingJobs.clear()

        releaseWakeLock()
        sessionActivity = null
    }

    @OptIn(FlowPreview::class)
    private fun collectSensors() {
        sensorRepository.trackPointFlow
            .sample(1000)
            .onEach {
                processTrackpoint(it)
        }.launchIn(applicationScope)
            .also { recordingJobs.add(it) }
    }

    private suspend fun persist() {
        sessionActivity?.let { dataRepository.updateActivity(it) }
    }

    // --- All data processing logic is now correctly placed here ---

    private suspend fun processTrackpoint(trackpoint: TrackPoint) {
        sessionActivity?.let { activity ->
            dataRepository.addTrack(trackpoint)
            processAltitude(trackpoint)
            processClimbrate(trackpoint)
            processPressure(trackpoint)
            processLocation(trackpoint)
        }
        persist()
    }

    private fun processAltitude(altitude: TrackPoint) {
        sessionActivity?.let { activity ->
            activity.maxAltitude = max(altitude.altitude, activity.maxAltitude)
            activity.minAltitude = min(altitude.altitude, activity.minAltitude)
            verticallyMoving.update(altitude.altitude)
            activity.ascent = verticallyMoving.getAscent()
            activity.descent = verticallyMoving.getDescent()
        }
    }

    private fun processClimbrate(climbrate: TrackPoint) {
        sessionActivity?.let { activity ->
            activity.maxClimbrate = max(climbrate.climbrate, activity.maxClimbrate)
            activity.minClimbrate = min(climbrate.climbrate, activity.minClimbrate)
        }
    }

    private fun processPressure(pressure: TrackPoint) {
        sessionActivity?.let { activity ->
            activity.maxPressure = max(pressure.pressure, activity.maxPressure)
            activity.minPressure = min(pressure.pressure, activity.minPressure)
        }
    }

    private fun processLocation(loc: TrackPoint) {
        sessionActivity?.let { activity ->
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
        }
    }

    private fun computeDistance(prev: TrackPoint?, current: TrackPoint): Float {
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
        val currentUid = authRepository.currentUser.first()?.uid ?: "anonymous"

        val activity = Activity(
            activityId = id,
            userId = currentUid,
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
