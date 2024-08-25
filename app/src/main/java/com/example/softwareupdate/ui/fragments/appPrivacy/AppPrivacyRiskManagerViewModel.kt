package com.example.softwareupdate.ui.fragments.appPrivacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.softwareupdate.adapters.privacymanager.PrivacyManagerEntity
import com.example.softwareupdate.di.usecase.GetAllPrivacyManagerAppsUseCase
import com.example.softwareupdate.utils.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppPrivacyRiskManagerViewModel @Inject constructor(private val getAllPrivacyManagerAppsUseCase: GetAllPrivacyManagerAppsUseCase) :
    ViewModel() {

    private val _appLists = MutableStateFlow<List<PrivacyManagerEntity?>>(arrayListOf())
    val appsLists: StateFlow<List<PrivacyManagerEntity?>> get() = _appLists

    private val state =
        MutableStateFlow<PrivacyManagerFragmentState>(PrivacyManagerFragmentState.Init)
    val mState: StateFlow<PrivacyManagerFragmentState> get() = state

    private fun setLoading() {
        state.value = PrivacyManagerFragmentState.IsLoading(true)
    }

    private fun hideLoading() {
        state.value = PrivacyManagerFragmentState.IsLoading(false)
    }

    private fun showToast(message: String) {
        state.value = PrivacyManagerFragmentState.ShowToast(message)
    }

    fun invokePrivacyManagerAppsUseCase() {
        viewModelScope.launch {
            getAllPrivacyManagerAppsUseCase.invokePrivacyManagerAppsUseCase().onStart {
                setLoading()
            }.catch { exception ->
                hideLoading()
                showToast(exception.message.toString())
                exception.printStackTrace()
            }.collect { result ->
                hideLoading()
                when (result) {
                    is DataState.Success -> {
                        val newData = result.data
                        if (newData != _appLists.value) {
                            _appLists.value = newData
                        }
                    }

                    else -> {
                        showToast("result.Some thing went Wrong")
                    }
                }
            }
        }
    }
}

sealed class PrivacyManagerFragmentState {
    object Init : PrivacyManagerFragmentState()
    data class IsLoading(val isLoading: Boolean) : PrivacyManagerFragmentState()
    data class ShowToast(val message: String) : PrivacyManagerFragmentState()
}
