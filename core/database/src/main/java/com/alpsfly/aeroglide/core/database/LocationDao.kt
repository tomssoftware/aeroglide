package com.alpsfly.aeroglide.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alpsfly.aeroglide.core.model.database.Location
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Query("select * from location")
    fun getAllLocation(): Flow<List<Location>>

    @Query("select * from location")
    fun getAllLocationAsFlow(): Flow<Location>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addLocation(location: Location)
}