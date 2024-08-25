package com.example.softwareupdate.di.repositories.languages

import android.content.Context
import com.example.softwareupdate.adapters.langauge.LanguageItemsModel
import com.example.softwareupdate.utils.DataState
import kotlinx.coroutines.flow.Flow

typealias FetchAllLanguagesFunction = (context: Context?) -> Flow<DataState<List<LanguageItemsModel>>>

interface LanguagesRepository {
    val allLanguages: FetchAllLanguagesFunction
}