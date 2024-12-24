package com.alpsfly.aeroglide.core.data.model.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alpsfly.aeroglide.core.data.model.local.Calibration

@Dao
interface CalibrationDao {

    @get:Query("select * from calibration order by timestamp DESC")
    val rows: LiveData<List<Calibration>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(calibration: Calibration)
}