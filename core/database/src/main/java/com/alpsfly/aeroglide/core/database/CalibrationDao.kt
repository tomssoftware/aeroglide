package com.alpsfly.aeroglide.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alpsfly.aeroglide.core.model.hardware.Calibration
import kotlinx.coroutines.flow.Flow

@Dao
interface CalibrationDao {
    @Query("select * from calibration")
    fun getAllCalibration(): Flow<List<Calibration>>

    @Query("select * from calibration order by timestamp limit 1")
    fun getLatestCalibration(): Flow<List<Calibration>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addCalibration(calibration: Calibration)
}
