package com.thermalscout.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "position")
data class Position(
        @PrimaryKey
        @ColumnInfo(name = "timestamp") var timestamp: Long,
        @ColumnInfo(name = "latitude") var latitude: Float,
        @ColumnInfo(name = "longitude") var longitude: Float,
        @ColumnInfo(name = "accuracy") var accuracy: Float
)