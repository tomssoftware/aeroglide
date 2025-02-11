package com.alpsfly.aeroglide.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alpsfly.aeroglide.core.model.database.Altitude
import kotlinx.coroutines.flow.Flow

@Dao
interface AltitudeDao {
    @get:Query("select * from altitude")
    val allAltitudes: Flow<List<Altitude>>

    @Query("select * from altitude where timestamp = :timestamp")
    fun getAltitude(timestamp: Long): Flow<Altitude>

    @Query("select * from altitude where timestamp between :start and :end")
    fun getAltitudesBetween(start: Long, end: Long): Flow<List<Altitude>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAltitude(altitude: Altitude)
}