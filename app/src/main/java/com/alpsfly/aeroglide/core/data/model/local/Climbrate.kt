package com.thermalscout.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "climbrate")
data class Climbrate(
        @PrimaryKey
        @ColumnInfo(name = "timestamp") var timestamp: Long,
        @ColumnInfo(name = "climbrate") var climbrate: Float,
        @ColumnInfo(name = "grade") var grade: Float
)