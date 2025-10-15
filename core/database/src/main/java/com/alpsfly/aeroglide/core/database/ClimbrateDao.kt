package com.alpsfly.aeroglide.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alpsfly.aeroglide.core.model.database.Climbrate
import kotlinx.coroutines.flow.Flow


@Dao
interface ClimbrateDao {
    @get:Query("select * from climbrate")
    val allClimbrates: Flow<List<Climbrate>>

    @Query("select * from climbrate where timestamp = :timestamp")
    fun getClimbrate(timestamp: Long): Flow<Climbrate?>

    @Query("select * from climbrate where timestamp between :start and :end")
    fun getClimbratesBetween(start: Long, end: Long): Flow<List<Climbrate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addClimbrate(climbrate: Climbrate)
}
