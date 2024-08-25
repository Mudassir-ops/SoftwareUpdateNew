package com.example.softwareupdate.di.usecase

import android.content.Context
import com.example.softwareupdate.adapters.version.VersionsEntity
import com.example.softwareupdate.di.repositories.version.VersionsRepository
import com.example.softwareupdate.utils.DataState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllAndroidVersionsUseCase @Inject constructor(private val versionsRepository: VersionsRepository) {
    fun invokeAllAppVersionsUseCase(context: Context?): Flow<DataState<List<VersionsEntity>>> {
        return versionsRepository.fetchAllVersions(context)
    }
}