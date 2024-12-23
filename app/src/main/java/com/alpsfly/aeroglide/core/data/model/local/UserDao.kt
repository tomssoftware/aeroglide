package com.thermalscout.database

/**
 * Created by Thomas on 19.02.2018.
 */

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UserDao {

    @get:Query("select * from user")
    val allUsers: LiveData<List<User>>

    @get:Query("select * from user")
    val userList: List<User>

    @Query("select * from user where user_id = :id")
    fun getUser(id: String): List<User>

    @Query("select * from user where user_id = :userId")
    fun userLiveDataWhere(userId: Long): LiveData<List<User>>

    @Query("select * from user where user_id = :userId")
    fun userListWhere(userId: Long): List<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addUser(user: User)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateUser(user: User)

    @Query("delete from user")
    fun removeAllUsers()
}
