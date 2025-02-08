package com.alpsfly.aeroglide.core.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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
    @ColumnInfo(name = "max_heartrate") var maxHeartrate: Long = Long.MIN_VALUE,
    @ColumnInfo(name = "min_heartrate") var minHeartrate: Long = Long.MAX_VALUE
)