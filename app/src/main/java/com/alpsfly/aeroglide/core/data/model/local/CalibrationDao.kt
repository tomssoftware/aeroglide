package com.thermalscout.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CalibrationDao {

    @get:Query("select * from calibration order by timestamp DESC")
    val rows: LiveData<List<Calibration>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(calibration: Calibration)
}