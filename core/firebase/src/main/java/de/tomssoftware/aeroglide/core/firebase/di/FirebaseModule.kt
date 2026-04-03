package de.tomssoftware.aeroglide.core.firebase.di

import de.tomssoftware.aeroglide.core.firebase.CloudStorage
import de.tomssoftware.aeroglide.core.firebase.CloudStorageFirebase
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

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
}

/** Binds the [CloudStorage] interface to its Firebase implementation. */
@Module
@InstallIn(SingletonComponent::class)
interface CloudStorageModule {
    @Singleton
    @Binds
    fun bindCloudStorage(impl: CloudStorageFirebase): CloudStorage
}

