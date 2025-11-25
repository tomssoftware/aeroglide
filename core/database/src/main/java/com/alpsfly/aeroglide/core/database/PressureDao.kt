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
    suspend fun addPressure(pressure: Pressure)

    @Query("select * from pressure where timestamp between :start and :end")
    fun getPressuresBetween(start: Long, end: Long): Flow<List<Pressure>>
}
