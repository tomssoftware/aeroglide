package de.tomssoftware.aeroglide.core.model.firebase

import de.tomssoftware.aeroglide.core.model.database.SyncState
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

import de.tomssoftware.aeroglide.core.model.database.Activity as ActivityRlo

/**
 * Firestore representation of an Activity.
 * Path: /users/{uid}/activities/{activityId}
 */
@IgnoreExtraProperties
data class Activity(
    @DocumentId
    val id: String? = null,

    @get:PropertyName("user_id")
    val userId: String = "",

    @get:PropertyName("begin")
    val begin: Long = 0L,

    @get:PropertyName("end")
    val end: Long = 0L,

    @get:PropertyName("distance")
    val distance: Float = 0f,

    @get:PropertyName("duration")
    val duration: Long = 0L,

    @get:PropertyName("ascent")
    val ascent: Float = 0f,

    @get:PropertyName("descent")
    val descent: Float = 0f,

    @get:PropertyName("min_pressure")
    val minPressure: Float = 0f,

    @get:PropertyName("max_pressure")
    val maxPressure: Float = 0f,

    @get:PropertyName("min_altitude")
    val minAltitude: Float = 0f,

    @get:PropertyName("max_altitude")
    val maxAltitude: Float = 0f,

    @get:PropertyName("min_speed")
    val minSpeed: Float = 0f,

    @get:PropertyName("max_speed")
    val maxSpeed: Float = 0f,

    @get:PropertyName("avg_speed")
    val avgSpeed: Float = 0f,

    @get:PropertyName("max_climbrate")
    val maxClimbrate: Float = 0f,

    @get:PropertyName("min_climbrate")
    val minClimbrate: Float = 0f,

    @get:PropertyName("positive_avg_climbrate")
    val positiveAvgClimbrate: Float = 0f,

    @get:PropertyName("negative_avg_climbrate")
    val negativeAvgClimbrate: Float = 0f,

    @get:PropertyName("max_grade")
    val maxGrade: Float = 0f,

    @get:PropertyName("min_grade")
    val minGrade: Float = 0f,

    @get:PropertyName("max_heart_rate")
    val maxHeartRate: Long = 0L,

    @get:PropertyName("min_heart_rate")
    val minHeartRate: Long = 0L,

    @get:PropertyName("updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Extension function to convert a Firestore DTO back into a local Room Entity.
 * This is used when downloading data from the cloud to the local database.
 */
fun Activity.toDatabase(): ActivityRlo {
    return ActivityRlo(
        // We map the Firestore Document ID back to the firestore_id column
        firestoreId = this.id,
        userId = this.userId,
        begin = this.begin,
        end = this.end,
        distance = this.distance,
        duration = this.duration,
        ascent = this.ascent,
        descent = this.descent,
        minPressure = this.minPressure,
        maxPressure = this.maxPressure,
        minAltitude = this.minAltitude,
        maxAltitude = this.maxAltitude,
        minSpeed = this.minSpeed,
        maxSpeed = this.maxSpeed,
        avgSpeed = this.avgSpeed,
        maxClimbrate = this.maxClimbrate,
        minClimbrate = this.minClimbrate,
        positiveAvgClimbrate = this.positiveAvgClimbrate,
        negativeAvgClimbrate = this.negativeAvgClimbrate,
        maxGrade = this.maxGrade,
        minGrade = this.minGrade,
        maxHeartRate = this.maxHeartRate,
        minHeartRate = this.minHeartRate,

        // Sync Metadata for local tracking
        syncState = SyncState.SYNCED,
        lastSyncedAt = System.currentTimeMillis()
    )
}


