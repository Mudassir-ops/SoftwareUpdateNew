package com.example.softwareupdate.ui.fragments.language

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.softwareupdate.adapters.langauge.LanguageItemsModel
import com.example.softwareupdate.di.usecase.GetAllLanguagesUseCase
import com.example.softwareupdate.utils.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(private val languagesUseCase: GetAllLanguagesUseCase) :
    ViewModel() {

    private val _languageLists = MutableStateFlow<List<LanguageItemsModel?>>(arrayListOf())
    val languageLists: StateFlow<List<LanguageItemsModel?>> get() = _languageLists

    private val state = MutableStateFlow<LanguageFragmentState>(LanguageFragmentState.Init)
    val mState: StateFlow<LanguageFragmentState> get() = state

    private fun setLoading() {
        state.value = LanguageFragmentState.IsLoading(true)
    }

    private fun hideLoading() {
        state.value = LanguageFragmentState.IsLoading(false)
    }

    private fun showToast(message: String) {
        state.value = LanguageFragmentState.ShowToast(message)
    }

    fun getAllUpdatedApp(context: Context) {
        viewModelScope.launch {
            languagesUseCase.invokeAllLanguageUseCase(context).onStart {
                setLoading()
            }.catch { exception ->
                hideLoading()
                showToast(exception.message.toString())
                exception.printStackTrace()
            }.collect { result ->
                hideLoading()
                when (result) {
                    is DataState.Success -> {
                        _languageLists.value = result.data
                    }

                    else -> {
                        showToast("result.Some thing went Wrong")
                    }
                }
            }
        }
    }
}

sealed class LanguageFragmentState {
    object Init : LanguageFragmentState()
    data class IsLoading(val isLoading: Boolean) : LanguageFragmentState()
    data class ShowToast(val message: String) : LanguageFragmentState()
}