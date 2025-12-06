package com.alpsfly.aeroglide.core.data

//import com.alpsfly.aeroglide.core.firebase.UserRds
import jakarta.inject.Inject

interface RemoteDataRepositoryInterface {
    //suspend fun addUser(userId: String, user: User): Result<Unit>

}

class RemoteDataRepository @Inject constructor(
    //private val userRds: UserRds,
) : RemoteDataRepositoryInterface {
//    override suspend fun addUser(userId: String, user: User): Result<Unit> {
//        return userRds.addUser(userId, user)
//    }
}