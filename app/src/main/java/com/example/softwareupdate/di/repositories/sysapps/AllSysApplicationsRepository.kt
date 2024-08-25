package com.example.softwareupdate.di.repositories.sysapps

import com.example.softwareupdate.adapters.sysapps.MySysAppsEntity
import com.example.softwareupdate.utils.DataState
import kotlinx.coroutines.flow.Flow


typealias allSysAppsListFunction = () -> Flow<DataState<List<MySysAppsEntity>>>

interface AllSysApplicationsRepository {
    val allSysAppsList: allSysAppsListFunction
}