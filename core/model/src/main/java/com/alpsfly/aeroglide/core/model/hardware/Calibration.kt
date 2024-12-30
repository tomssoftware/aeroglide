package com.alpsfly.aeroglide.core.model.hardware

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity(tableName = "calibration")
data class Calibration(
    @PrimaryKey
    val timestamp: Long = 0,
    var isCalibrated: Boolean = false,
    var altitude0: Float = 0f,
    var pressure0: Float = 0f,
    var verticalAccuracy: Float = 0f,
    var horizontalAccuracy: Float = 0f
)
