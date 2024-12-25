/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alpsfly.aeroglide.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import com.alpsfly.aeroglide.core.data.LocalDataRepository
import com.alpsfly.aeroglide.core.model.common.User
import com.alpsfly.aeroglide.core.database.UserDao

/**
 * Unit tests for [LocalDataRepository].
 */
@OptIn(ExperimentalCoroutinesApi::class) // TODO: Remove when stable
class LocalDataRepositoryTest {

    @Test
    fun aeroGlides_newItemSaved_itemIsReturned() = runTest {
        val repository = LocalDataRepository(FakeUserDao())

        repository.addUser(
            User(
                "1",
                "Test",
                "Test",
                "Test",
                "Test",
                "Test",
                "Test"
            )
        )

        assertEquals(repository.users.first().size, 1)
    }

}

private class FakeUserDao : UserDao {

    private val data = mutableListOf<User>()

    override fun getAllUsers(): Flow<List<User>> {
        TODO("Not yet implemented")
    }

    override fun getUser(id: String): Flow<List<User>> {
        TODO("Not yet implemented")
    }

    override fun addUser(user: User) {
        data.add(0, user)
    }

    override fun updateUser(user: User) {
        TODO("Not yet implemented")
    }

    override fun removeAllUsers() {
        TODO("Not yet implemented")
    }
}
