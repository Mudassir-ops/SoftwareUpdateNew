package com.example.softwareupdate.di.usecase

import com.example.softwareupdate.adapters.allapps.PackageInfoEntity
import com.example.softwareupdate.di.repositories.checkupdaterepository.CheckUpdateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckAppUpdateUseCase @Inject constructor(
    private val checkUpdateRepository: CheckUpdateRepository,
) {
    suspend fun invokeCheckAppUpdateCount(
        callback: (Int, String, Int, Int, PackageInfoEntity) -> Unit,
        callbackFinished: (Boolean) -> Unit
    ): Int {
        return checkUpdateRepository.checkAppUpdateCount(callback = { it, name, checkCount, totalAppCount, appInfo ->
            callback.invoke(it, name, checkCount, totalAppCount, appInfo)
        }, callbackFinished = {
            callbackFinished.invoke(it)
        })
    }

    suspend fun insertUpdatedApp(packageInfoEntity: com.example.softwareupdate.data.PackageInfoEntity) {
        checkUpdateRepository.insertUpdatedApp(packageInfoEntity)
    }

    fun getAllUpdatedApp(): Flow<List<com.example.softwareupdate.data.PackageInfoEntity>> {
        return checkUpdateRepository.getAllUpdatedApp()
    }

    suspend fun getRowCount(): Int {
        return checkUpdateRepository.getRowCount()
    }

    suspend fun deleteAllTables() {
        checkUpdateRepository.deleteAllTables()
    }
}