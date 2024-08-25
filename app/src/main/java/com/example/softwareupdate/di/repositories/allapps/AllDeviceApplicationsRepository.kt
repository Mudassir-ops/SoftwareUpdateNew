package com.example.softwareupdate.di.repositories.allapps

import com.example.softwareupdate.adapters.allapps.AllAppsEntity
import com.example.softwareupdate.utils.DataState
import kotlinx.coroutines.flow.Flow

typealias AllDeviceAppsFunction = () -> Flow<DataState<List<AllAppsEntity>>>

interface AllDeviceApplicationsRepository {
    val allDeviceApps: AllDeviceAppsFunction
}