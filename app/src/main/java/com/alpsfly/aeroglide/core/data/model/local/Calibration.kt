package com.thermalscout.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calibration")
data class Calibration(
    @PrimaryKey
    @ColumnInfo(name = "timestamp") var timestamp: Long = 0L,
    @ColumnInfo(name = "automatic") var automatic: Boolean = true,
    @ColumnInfo(name = "calibration_altitude") var calibrationAltitude: Float = 0f,
    @ColumnInfo(name = "calibration_pressure") var calibrationPressure: Float = 0f,
    @ColumnInfo(name = "gps_accuracy") var gpsAccuracy: Float = 0f,
    @ColumnInfo(name = "gps_altitude") var gpsAltitude: Float = 0f
)