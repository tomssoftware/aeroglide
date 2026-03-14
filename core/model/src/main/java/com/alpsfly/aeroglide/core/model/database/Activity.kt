package com.alpsfly.aeroglide.core.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

import com.alpsfly.aeroglide.core.model.firebase.Activity as ActivityDto

@Entity(tableName = "activity")
data class Activity (
    @PrimaryKey
    @ColumnInfo(name = "activity_id") var activityId: Long = 0L,
    @ColumnInfo(name = "user_id") var userId: String = "",
    @ColumnInfo(name = "begin") var begin: Long = 0L,
    @ColumnInfo(name = "end") var end: Long = 0L,
    @ColumnInfo(name = "distance") var distance: Float = 0f,
    @ColumnInfo(name = "duration") var duration: Long = 0L,
    @ColumnInfo(name = "ascent") var ascent: Float = 0f,
    @ColumnInfo(name = "descent") var descent: Float = 0f,
    @ColumnInfo(name = "min_pressure") var minPressure: Float = Float.MAX_VALUE,
    @ColumnInfo(name = "max_pressure") var maxPressure: Float = Float.MIN_VALUE,
    @ColumnInfo(name = "min_altitude") var minAltitude: Float = Float.MAX_VALUE,
    @ColumnInfo(name = "max_altitude") var maxAltitude: Float = Float.MIN_VALUE,
    @ColumnInfo(name = "min_speed") var minSpeed: Float = Float.MAX_VALUE,
    @ColumnInfo(name = "max_speed") var maxSpeed: Float = Float.MIN_VALUE,
    @ColumnInfo(name = "avg_speed") var avgSpeed: Float = 0f,
    @ColumnInfo(name = "max_climbrate") var maxClimbrate: Float = Float.MIN_VALUE,
    @ColumnInfo(name = "min_climbrate") var minClimbrate: Float = Float.MAX_VALUE,
    @ColumnInfo(name = "positive_avg_climbrate") var positiveAvgClimbrate: Float = 0f,
    @ColumnInfo(name = "negative_avg_climbrate") var negativeAvgClimbrate: Float = 0f,
    @ColumnInfo(name = "max_grade") var maxGrade: Float = Float.MIN_VALUE,
    @ColumnInfo(name = "min_grade") var minGrade: Float = Float.MAX_VALUE,
    @ColumnInfo(name = "max_heart_rate") var maxHeartRate: Long = Long.MIN_VALUE,
    @ColumnInfo(name = "min_heart_rate") var minHeartRate: Long = Long.MAX_VALUE,

    // Fields to track the synchronization state with Firebase
    @ColumnInfo(name = "firestore_id") var firestoreId: String? = null,
    @ColumnInfo(name = "sync_state") var syncState: SyncState = SyncState.LOCAL,
    @ColumnInfo(name = "last_synced_at") var lastSyncedAt: Long = 0,
    @ColumnInfo(name = "sync_error") var syncError: String? = null
)

// In your local Activity.kt or a Mapper class
fun Activity.toFirestore(): ActivityDto {
    return ActivityDto(
        id = this.firestoreId, // Use existing ID if we are updating
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
        updatedAt = System.currentTimeMillis()
    )
}
