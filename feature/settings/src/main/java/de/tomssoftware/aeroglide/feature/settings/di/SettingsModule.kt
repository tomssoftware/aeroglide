package de.tomssoftware.aeroglide.feature.settings.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingsModule { // You can name this object whatever you like, e.g., StorageModule

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        // You can name your shared preferences file whatever you want
        return context.getSharedPreferences("AeroGlidePrefs", Context.MODE_PRIVATE)
    }
}
