package com.alpsfly.aeroglide.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alpsfly.aeroglide.core.model.database.Altitude
import kotlinx.coroutines.flow.Flow

@Dao
interface AltitudeDao {
    @Query("select * from altitude")
    fun getAllAltitude(): Flow<List<Altitude>>

    @Query("select * from altitude")
    fun getAllAltitudeAsFlow(): Flow<Altitude>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAltitude(altitude: Altitude)
}