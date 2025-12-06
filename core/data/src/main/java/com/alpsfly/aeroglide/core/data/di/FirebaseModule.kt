package com.alpsfly.aeroglide.core.data.di

import com.alpsfly.aeroglide.core.data.RemoteDataRepository
import com.alpsfly.aeroglide.core.data.RemoteDataRepositoryInterface
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Module
    @InstallIn(SingletonComponent::class)
    interface RemoteDataRepositoryModule {
        @Singleton
        @Binds
        fun bindsRemoteDataRepository(
            remoteDataRepository: RemoteDataRepository
        ): RemoteDataRepositoryInterface
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    // Since you mentioned syncing with Cloud Storage/Firestore later,
    // it's good practice to provide these here as well:

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
}
