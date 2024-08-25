package com.example.softwareupdate.di.usecase

import com.example.softwareupdate.adapters.sysapps.MySysAppsEntity
import com.example.softwareupdate.di.repositories.sysapps.AllSysApplicationsRepository
import com.example.softwareupdate.utils.DataState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllSysAppsUseCase @Inject constructor(private val allSysApplicationsRepository: AllSysApplicationsRepository) {
    fun invokeAllSysAppsUseCase(): Flow<DataState<List<MySysAppsEntity>>> {
        return allSysApplicationsRepository.allSysAppsList()
    }
}