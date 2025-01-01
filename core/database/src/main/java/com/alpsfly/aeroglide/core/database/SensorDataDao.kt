package com.alpsfly.aeroglide.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alpsfly.aeroglide.core.model.hardware.SensorData
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorDataDao {
    @Query("select * from sensor_data")
    fun getAllSensorData(): Flow<List<SensorData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addSensorData(sensorData: SensorData)
}