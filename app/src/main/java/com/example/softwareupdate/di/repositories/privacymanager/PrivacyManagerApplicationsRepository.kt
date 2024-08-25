package com.example.softwareupdate.di.repositories.privacymanager

import com.example.softwareupdate.adapters.privacymanager.PrivacyManagerEntity
import com.example.softwareupdate.utils.DataState
import kotlinx.coroutines.flow.Flow

typealias AllDeviceAppsFunction = () -> Flow<DataState<List<PrivacyManagerEntity?>>>

interface PrivacyManagerApplicationsRepository {
    val allDeviceAppsWithPermissions: AllDeviceAppsFunction
}