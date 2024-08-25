package com.example.softwareupdate.di.usecase

import android.content.Context
import com.example.softwareupdate.adapters.langauge.LanguageItemsModel
import com.example.softwareupdate.di.repositories.languages.LanguagesRepository
import com.example.softwareupdate.utils.DataState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllLanguagesUseCase @Inject constructor(private val languagesRepository: LanguagesRepository) {
    fun invokeAllLanguageUseCase(context: Context?): Flow<DataState<List<LanguageItemsModel>>> {
        return languagesRepository.allLanguages(context)
    }
}