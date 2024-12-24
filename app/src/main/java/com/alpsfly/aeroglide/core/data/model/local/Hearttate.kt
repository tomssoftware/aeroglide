package com.alpsfly.aeroglide.core.data.model.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "heartrate")
data class Heartrate(
    @PrimaryKey
    @ColumnInfo(name = "timestamp") var timestamp: Long,
    @ColumnInfo(name = "heartrate") var heartrate: Float
)