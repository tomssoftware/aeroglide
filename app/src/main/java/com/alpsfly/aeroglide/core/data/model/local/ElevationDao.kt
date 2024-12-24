package com.alpsfly.aeroglide.core.data.model.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface ElevationDao {
    @get:Query("select elevation_timestamp as timestamp, elevation from track_log")
    val allElevation: LiveData<List<Elevation>>
}
