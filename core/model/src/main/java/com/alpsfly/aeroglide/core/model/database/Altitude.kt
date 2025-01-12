package com.alpsfly.aeroglide.core.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "altitude")
data class Altitude(
    @PrimaryKey
    @ColumnInfo(name = "timestamp") var timestamp: Long = 0L,
    @ColumnInfo(name = "frequency") var frequency: Float = 0f,
    @ColumnInfo(name = "altitude") var altitude: Float = 0f
)