package com.thermalscout.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track_log")
data class TrackLog(
    @PrimaryKey
    @ColumnInfo(name = "track_log_id") var trackLogId: Long,
    @ColumnInfo(name = "track_id") var trackId: Long,
    @ColumnInfo(name = "air_pressure_timestamp") var airPressureTimestamp: Long,
    @ColumnInfo(name = "air_pressure") var airPressure: Float,
    @ColumnInfo(name = "altitude_timestamp") var altitudeTimestamp: Long,
    @ColumnInfo(name = "altitude") var altitude: Float,
    @ColumnInfo(name = "aviation_timestamp") var aviationTimestamp: Long,
    @ColumnInfo(name = "ascent") var ascent: Float,
    @ColumnInfo(name = "descent") var descent: Float,
    @ColumnInfo(name = "climbrate_timestamp") var climbrateTimestamp: Long,
    @ColumnInfo(name = "climbrate") var climbrate: Float,
    @ColumnInfo(name = "glideratio") var glideRatio: Float,
    @ColumnInfo(name = "grade") var grade: Float,
    @ColumnInfo(name = "elevation_timestamp") var elevationTimestamp: Long,
    @ColumnInfo(name = "elevation") var elevation: Short,
    @ColumnInfo(name = "compass_timestamp") var compassTimestamp: Long,
    @ColumnInfo(name = "azimuth") var azimuth: Float,
    @ColumnInfo(name = "position_timestamp") var positionTimestamp: Long,
    @ColumnInfo(name = "accuracy") var accuracy: Float,
    @ColumnInfo(name = "latitude") var latitude: Double,
    @ColumnInfo(name = "longitude") var longitude: Double,
    @ColumnInfo(name = "gps_altitude") var gpsAltitude: Float,
    @ColumnInfo(name = "velocity_timestamp") var velocityTimestamp: Long,
    @ColumnInfo(name = "velocity") var velocity: Float,
    @ColumnInfo(name = "bearing") var bearing: Float,
    @ColumnInfo(name = "wind_indication_timestamp") var windIndicationTimestamp: Long,
    @ColumnInfo(name = "wind_speed") var windSpeed: Float,
    @ColumnInfo(name = "wind_direction") var windDirection: Float,
    @ColumnInfo(name = "heartrate_timestamp") var heartTimestamp: Long,
    @ColumnInfo(name = "heartrate") var heartrate: Long
) {
    constructor() : this(
        0L, 0L,
        0L, 0f,
        0L, 0f,
        0L, 0f, 0f,
        0L, 0f, 0f, 0f,
        0L, 0,
        0L, 0f,
        0L, 0f, 0.0, 0.0, 0f,
        0L, 0f, 0f,
        0L, 0f, 0f,
        0, 0L
    )
}