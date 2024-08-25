package com.example.softwareupdate.di

import android.content.Context
import androidx.room.Room
import com.example.softwareupdate.data.UpdateSoftwareDatabase
import com.example.softwareupdate.data.UpdatedAppDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {
    @Provides
    @Singleton
    fun provideUpdateSoftwareDatabase(@ApplicationContext context: Context): UpdateSoftwareDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            UpdateSoftwareDatabase::class.java,
            "update_software_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideUpdatedAppDao(database: UpdateSoftwareDatabase): UpdatedAppDao {
        return database.updatedAppDao()
    }

}