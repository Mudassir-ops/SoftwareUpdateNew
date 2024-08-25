package com.example.softwareupdate.di.repositories.version

import android.content.Context
import com.example.softwareupdate.adapters.version.VersionsEntity
import com.example.softwareupdate.utils.DataState
import kotlinx.coroutines.flow.Flow

typealias FetchAllVersionsFunction = (context: Context?) -> Flow<DataState<List<VersionsEntity>>>

interface VersionsRepository {
    val fetchAllVersions: FetchAllVersionsFunction
}