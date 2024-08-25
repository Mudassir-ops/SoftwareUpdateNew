package com.example.softwareupdate.di.repositories.languages

import android.content.Context
import com.example.softwareupdate.R
import com.example.softwareupdate.adapters.langauge.LanguageItemsModel
import com.example.softwareupdate.utils.DataState
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LanguagesRepositoryImpl @Inject constructor(
    private val context: Context,
) : LanguagesRepository {

    override val allLanguages: FetchAllLanguagesFunction = { _ ->
        flow {
            val dataList = languageDetail()
            emit(DataState.Success(dataList))
        }
    }

    private fun languageDetail(): MutableList<LanguageItemsModel> {
        val versionsList = mutableListOf<LanguageItemsModel>()
        versionsList.add(
            LanguageItemsModel(
                itemImage = R.drawable.upside_down_cake,
                lbOne = "English", isSelected = true
            )
        )
        versionsList.add(
            LanguageItemsModel(
                itemImage = R.drawable.upside_down_cake,
                lbOne = "Chinese", isSelected = false
            )
        )

        versionsList.add(
            LanguageItemsModel(
                itemImage = R.drawable.upside_down_cake,
                lbOne = "Turkish", isSelected = false
            )
        )
        versionsList.add(
            LanguageItemsModel(
                itemImage = R.drawable.upside_down_cake,
                lbOne = "German", isSelected = false
            )
        )

        versionsList.add(
            LanguageItemsModel(
                itemImage = R.drawable.upside_down_cake,
                lbOne = "Arabic", isSelected = false
            )
        )
        versionsList.add(
            LanguageItemsModel(
                itemImage = R.drawable.upside_down_cake,
                lbOne = "Korean", isSelected = false
            )
        )

        versionsList.add(
            LanguageItemsModel(
                itemImage = R.drawable.upside_down_cake,
                lbOne = "French", isSelected = false
            )
        )

        versionsList.add(
            LanguageItemsModel(
                itemImage = R.drawable.upside_down_cake,
                lbOne = "Spanish", isSelected = false
            )
        )

        versionsList.add(
            LanguageItemsModel(
                itemImage = R.drawable.upside_down_cake,
                lbOne = "Portuguese", isSelected = false
            )
        )

        return versionsList
    }

}