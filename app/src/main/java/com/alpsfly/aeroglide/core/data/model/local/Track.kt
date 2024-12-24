package com.alpsfly.aeroglide.core.data.model.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Thomas on 25.02.2018.
 */

@Entity(tableName = "track")
data class Track(
        @PrimaryKey
        @ColumnInfo(name = "track_id") var trackId: Long,
        @ColumnInfo(name = "user_id") var userId: String,
        @ColumnInfo(name = "name") var name: String,
        @ColumnInfo(name = "area") var area: String,
        @ColumnInfo(name = "departure_date") var departureDate: Long,
        @ColumnInfo(name = "departure_time") var departureTime: Long,
        @ColumnInfo(name = "distance") var distance: Float,
        @ColumnInfo(name = "duration") var duration: Long,
        @ColumnInfo(name = "lat") var lat: Double,
        @ColumnInfo(name = "lon") var lon: Double,
        @ColumnInfo(name = "ascent") var ascent: Float,
        @ColumnInfo(name = "descent") var descent: Float,
        @ColumnInfo(name = "min_altitude") var minAltitude: Float,
        @ColumnInfo(name = "max_altitude") var maxAltitude: Float,
        @ColumnInfo(name = "min_speed") var minSpeed: Float,
        @ColumnInfo(name = "max_speed") var maxSpeed: Float,
        @ColumnInfo(name = "max_climbrate") var maxClimbrate: Float,
        @ColumnInfo(name = "min_climbrate") var minClimbrate: Float,
        @ColumnInfo(name = "max_climbrate_int") var maxClimbrateInt: Float,
        @ColumnInfo(name = "min_climbrate_int") var minClimbrateInt: Float,
        @ColumnInfo(name = "max_grade") var maxGrade: Float,
        @ColumnInfo(name = "min_grade") var minGrade: Float,
        @ColumnInfo(name = "max_heartrate") var maxHeartrate: Long,
        @ColumnInfo(name = "min_heartrate") var minHeartrate: Long
) {
    constructor() : this(
            0,
            "",
            "",
            "",
            System.currentTimeMillis(),
            System.currentTimeMillis(),
            0f,
            0,
            0.0,
            0.0,
            0f,
            0f,
            0f,
            0f,
            0f,
            0f,
            0f,
            0f,
            0f,
            0f,
            0f,
            0f,
            0L,
            0L)
}
