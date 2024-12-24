package com.alpsfly.aeroglide.core.data.model.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "velocity")
data class Velocity(
    @PrimaryKey
    @ColumnInfo(name = "timestamp") var timestamp: Long,
    @ColumnInfo(name = "velocity") var velocity: Float
)