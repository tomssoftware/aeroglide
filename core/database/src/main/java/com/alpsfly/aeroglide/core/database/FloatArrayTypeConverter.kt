package com.alpsfly.aeroglide.core.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FloatArrayTypeConverter {

    private val gson = Gson()

    @TypeConverter
    fun fromString(data: String?): FloatArray? {
        if (data == null) {
            return null
        }
        val listType = object : TypeToken<List<Float>>() {}.type
        val list = gson.fromJson<List<Float>>(data, listType)
        return if (list != null && list.size == 3) {
            floatArrayOf(list[0], list[1], list[2])
        } else {
            null
        }
    }

    @TypeConverter
    fun toString(array: FloatArray?): String? {
        return if (array != null && array.size in (1..3)) {
            gson.toJson(array.toList())
        } else {
            null
        }
    }
}