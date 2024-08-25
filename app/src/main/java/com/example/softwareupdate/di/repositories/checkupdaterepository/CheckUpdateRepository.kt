package com.example.softwareupdate.di.repositories.checkupdaterepository

import com.example.softwareupdate.adapters.allapps.PackageInfoEntity
import com.example.softwareupdate.data.UpdatedAppDao


interface CheckUpdateRepository {

    suspend fun checkAppUpdateCount(
        callback: (Int, String, Int, Int, PackageInfoEntity) -> Unit,
        callbackFinished: (Boolean) -> Unit
    ): Int

    suspend fun insertUpdatedApp(packageInfoEntity: com.example.softwareupdate.data.PackageInfoEntity)

    fun getAllUpdatedApp(): kotlinx.coroutines.flow.Flow<List<com.example.softwareupdate.data.PackageInfoEntity>>

    suspend fun getRowCount(): Int

    suspend fun deleteAllTables()

}