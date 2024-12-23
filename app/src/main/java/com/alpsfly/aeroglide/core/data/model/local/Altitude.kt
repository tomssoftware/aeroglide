package com.thermalscout.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "altitude")
data class Altitude(
    @PrimaryKey
    @ColumnInfo(name = "timestamp") var timestamp: Long,
    @ColumnInfo(name = "altitude") var altitude: Float
)