package com.alpsfly.aeroglide.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alpsfly.aeroglide.core.model.database.Pressure
import kotlinx.coroutines.flow.Flow

@Dao
interface PressureDao {
    @Query("select * from pressure")
    fun getAllPressure(): Flow<List<Pressure>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addPressure(pressure: Pressure)
}