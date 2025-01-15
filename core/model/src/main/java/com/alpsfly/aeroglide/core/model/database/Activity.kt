package com.alpsfly.aeroglide.core.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity")
data class Activity (
    @PrimaryKey
    @ColumnInfo(name = "activity_id") var trackId: Long = 0L,
    @ColumnInfo(name = "user_id") var userId: String = "",
    @ColumnInfo(name = "begin") var begin: Long = 0L,
    @ColumnInfo(name = "end") var end: Long = 0L,
    @ColumnInfo(name = "distance") var distance: Float = 0f,
    @ColumnInfo(name = "duration") var duration: Long = 0L,
    @ColumnInfo(name = "ascent") var ascent: Float = 0f,
    @ColumnInfo(name = "descent") var descent: Float = 0f,
    @ColumnInfo(name = "min_altitude") var minAltitude: Float = 0f,
    @ColumnInfo(name = "max_altitude") var maxAltitude: Float = 0f,
    @ColumnInfo(name = "min_speed") var minSpeed: Float = 0f,
    @ColumnInfo(name = "max_speed") var maxSpeed: Float = 0f,
    @ColumnInfo(name = "max_climbrate") var maxClimbrate: Float = 0f,
    @ColumnInfo(name = "min_climbrate") var minClimbrate: Float = 0f,
    @ColumnInfo(name = "max_grade") var maxGrade: Float = 0f,
    @ColumnInfo(name = "min_grade") var minGrade: Float = 0f,
    @ColumnInfo(name = "max_heartrate") var maxHeartrate: Long = 0L,
    @ColumnInfo(name = "min_heartrate") var minHeartrate: Long = 0L
)