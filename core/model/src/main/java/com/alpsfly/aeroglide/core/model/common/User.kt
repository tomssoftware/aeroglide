package com.alpsfly.aeroglide.core.model.common

/**
 * Created by Thomas on 19.02.2018.
 */

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey
    @ColumnInfo(name = "user_id") var userId: String,
    @ColumnInfo(name = "fore_name") var foreName: String,
    @ColumnInfo(name = "last_name") var lastName: String,
    @ColumnInfo(name = "licence") var licence: String,
    @ColumnInfo(name = "glider") var glider: String,
    @ColumnInfo(name = "phone") var phone: String,
    @ColumnInfo(name = "email") var email: String
)
