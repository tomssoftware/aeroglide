package de.tomssoftware.aeroglide.core.data

//import de.tomssoftware.aeroglide.core.firebase.UserRds
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