package com.alpsfly.aeroglide.core.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location")
data class Location(
    @PrimaryKey
    @ColumnInfo(name = "timestamp") var timestamp: Long = 0L,
    @ColumnInfo(name = "latitude") var latitude: Float = 0f,
    @ColumnInfo(name = "longitude") var longitude: Float = 0f,
    @ColumnInfo(name = "altitude") var altitude: Float = 0f,
    @ColumnInfo(name = "bearing") var bearing: Float = 0f,
    @ColumnInfo(name = "speed") var speed: Float = 0f,
    @ColumnInfo(name = "horizontal_accuracy") var horizontalAccuracy: Float = 0f,
    @ColumnInfo(name = "vertical_accuracy") var verticalAccuracy: Float = 0f,
    @ColumnInfo(name = "bearing_accuracy") var bearingAccuracy: Float = 0f,
    @ColumnInfo(name = "speed_accuracy") var speedAccuracy: Float = 0f,
    @ColumnInfo(name = "provider") var provider: String = "unknown"
)