package com.example.softwareupdate.di.usecase

import com.example.softwareupdate.adapters.privacymanager.PrivacyManagerEntity
import com.example.softwareupdate.di.repositories.privacymanager.PrivacyManagerApplicationsRepository
import com.example.softwareupdate.utils.DataState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllPrivacyManagerAppsUseCase @Inject constructor(private val privacyManagerApplicationsRepository: PrivacyManagerApplicationsRepository) {
    fun invokePrivacyManagerAppsUseCase(): Flow<DataState<List<PrivacyManagerEntity?>>> {
        return privacyManagerApplicationsRepository.allDeviceAppsWithPermissions()
    }

}