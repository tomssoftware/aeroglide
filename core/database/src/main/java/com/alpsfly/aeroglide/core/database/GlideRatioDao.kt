package com.alpsfly.aeroglide.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import com.alpsfly.aeroglide.core.model.database.GlideRatio

@Dao
interface GlideRatioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addGlideRatio(glideRatio: GlideRatio)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateGlideRatio(glideRatio: GlideRatio)
}