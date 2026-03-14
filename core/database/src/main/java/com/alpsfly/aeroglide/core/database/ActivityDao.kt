package com.alpsfly.aeroglide.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.Update
import com.alpsfly.aeroglide.core.model.database.Activity
import com.alpsfly.aeroglide.core.model.database.SyncState
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("select * from activity")
    fun allActivitiesFlow(): Flow<List<Activity>>

    @Query("SELECT * FROM activity WHERE activity_id = :activityId")
    fun getActivityFlow(activityId: Long): Flow<Activity?>

    @Query("SELECT * FROM activity WHERE activity_id = :activityId")
    suspend fun getActivity(activityId: Long): Activity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addActivity(activity: Activity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateActivity(activity: Activity)

    @Delete
    suspend fun deleteActivity(activity: Activity)
}
