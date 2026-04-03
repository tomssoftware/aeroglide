package de.tomssoftware.aeroglide.core.database

/**
 * Created by Thomas on 19.02.2018.
 */

import androidx.room.*
import de.tomssoftware.aeroglide.core.model.database.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("select * from user")
    fun getAllUsers(): Flow<List<User>>

    @Query("select * from user where user_id = :id")
    fun getUser(id: String): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUser(user: User)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUser(user: User)

    @Query("delete from user")
    suspend fun removeAllUsers()
}
