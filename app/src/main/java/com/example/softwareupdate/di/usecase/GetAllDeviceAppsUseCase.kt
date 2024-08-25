package com.example.softwareupdate.di.usecase

import com.example.softwareupdate.adapters.allapps.AllAppsEntity
import com.example.softwareupdate.di.repositories.allapps.AllDeviceApplicationsRepository
import com.example.softwareupdate.utils.DataState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllDeviceAppsUseCase @Inject constructor(private val allDeviceApplicationsRepository: AllDeviceApplicationsRepository) {
    fun invokeAllDeviceAppsUseCase(): Flow<DataState<List<AllAppsEntity>>> {
        return allDeviceApplicationsRepository.allDeviceApps()
    }

}