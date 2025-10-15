package com.alpsfly.aeroglide.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alpsfly.aeroglide.core.model.database.Location
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @get:Query("select * from location")
    val allLocations: Flow<List<Location>>

    @Query("select * from location where timestamp = :timestamp")
    fun getLocation(timestamp: Long): Flow<Location?>

    @Query("select * from location where timestamp between :start and :end")
    fun getLocationsBetween(start: Long, end: Long): Flow<List<Location>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addLocation(location: Location)
}