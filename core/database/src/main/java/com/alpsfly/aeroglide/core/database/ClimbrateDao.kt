package com.alpsfly.aeroglide.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alpsfly.aeroglide.core.model.database.Climbrate
import kotlinx.coroutines.flow.Flow


@Dao
interface ClimbrateDao {
    @Query("select * from climbrate")
    fun getAllClimbrate(): Flow<List<Climbrate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addClimbrate(climbrate: Climbrate)
}
