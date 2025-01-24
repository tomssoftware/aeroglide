package com.alpsfly.aeroglide.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.alpsfly.aeroglide.core.model.database.Activity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("select * from activity")
    fun getAllActivity(): Flow<List<Activity>>

    @Query("SELECT * FROM activity WHERE activity_id = :activityId")
    fun getActivity(activityId: Long): Flow<Activity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addActivity(activity: Activity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateActivity(activity: Activity)
}