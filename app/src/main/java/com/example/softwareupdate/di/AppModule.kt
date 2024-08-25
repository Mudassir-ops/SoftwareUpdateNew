package com.example.softwareupdate.di

import android.content.Context
import com.example.softwareupdate.data.UpdatedAppDao
import com.example.softwareupdate.di.repositories.allapps.AllDeviceApplicationsRepository
import com.example.softwareupdate.di.repositories.allapps.AllDeviceApplicationsRepositoryImpl
import com.example.softwareupdate.di.repositories.checkupdaterepository.CheckUpdateRepository
import com.example.softwareupdate.di.repositories.checkupdaterepository.CheckUpdateRepositoryImpl
import com.example.softwareupdate.di.repositories.languages.LanguagesRepository
import com.example.softwareupdate.di.repositories.languages.LanguagesRepositoryImpl
import com.example.softwareupdate.di.repositories.privacymanager.PrivacyManagerApplicationsRepository
import com.example.softwareupdate.di.repositories.privacymanager.PrivacyManagerApplicationsRepositoryImpl
import com.example.softwareupdate.di.repositories.sysapps.AllSysApplicationsRepository
import com.example.softwareupdate.di.repositories.sysapps.AllSysApplicationsRepositoryImpl
import com.example.softwareupdate.di.repositories.version.VersionsRepository
import com.example.softwareupdate.di.repositories.version.VersionsRepositoryImpl
import com.example.softwareupdate.ui.fragments.deviceInfo.DeviceInfoModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideVersionsRepository(
        @ApplicationContext context: Context
    ): VersionsRepository {
        return VersionsRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideSysAppsRepository(
        @ApplicationContext context: Context
    ): AllSysApplicationsRepository {
        return AllSysApplicationsRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideAllDeviceAppsRepository(
        @ApplicationContext context: Context
    ): AllDeviceApplicationsRepository {
        return AllDeviceApplicationsRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun providePrivacyManagerAppsRepository(
        @ApplicationContext context: Context
    ): PrivacyManagerApplicationsRepository {
        return PrivacyManagerApplicationsRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideAppUpdateRepository(
        @ApplicationContext context: Context?,
        updatedAppDao: UpdatedAppDao
    ): CheckUpdateRepository {
        return CheckUpdateRepositoryImpl(context, updatedAppDao)
    }

    @Provides
    @Singleton
    fun provideLanguageRepository(
        @ApplicationContext context: Context
    ): LanguagesRepository {
        return LanguagesRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun providesDeviceInfoModule(@ApplicationContext context: Context) = DeviceInfoModule(context)

}

