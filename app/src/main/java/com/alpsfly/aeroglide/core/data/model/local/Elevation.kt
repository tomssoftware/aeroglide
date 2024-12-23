package com.thermalscout.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "elevation")
class Elevation (
    @PrimaryKey
    @ColumnInfo(name = "timestamp") var timestamp: Long = 0L,
    @ColumnInfo(name = "elevation") var elevation: Short = 0
)