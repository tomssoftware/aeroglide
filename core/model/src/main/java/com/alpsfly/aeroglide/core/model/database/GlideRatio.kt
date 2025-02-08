package com.alpsfly.aeroglide.core.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "glide_ratio")
data class GlideRatio(
    @PrimaryKey
    @ColumnInfo(name = "timestamp") var timestamp: Long = 0L,
    @ColumnInfo(name = "frequency") var frequency: Float = 0f,
    @ColumnInfo(name = "glide_ratio") var glideRatio: Float = 0f
)
