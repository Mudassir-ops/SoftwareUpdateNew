package com.example.softwareupdate.ui.fragments.sysapps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.softwareupdate.adapters.sysapps.MySysAppsEntity
import com.example.softwareupdate.di.usecase.GetAllSysAppsUseCase
import com.example.softwareupdate.utils.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllSystemAppsViewModel @Inject constructor(private val useCaseSysApps: GetAllSysAppsUseCase) :
    ViewModel() {

    private val _sysAppsLists = MutableStateFlow<List<MySysAppsEntity?>>(arrayListOf())
    val sysAppsLists: StateFlow<List<MySysAppsEntity?>> get() = _sysAppsLists

    private val state = MutableStateFlow<AllSystemFragmentState>(AllSystemFragmentState.Init)
    val mState: StateFlow<AllSystemFragmentState> get() = state

    private fun setLoading() {
        state.value = AllSystemFragmentState.IsLoading(true)
    }

    private fun hideLoading() {
        state.value = AllSystemFragmentState.IsLoading(false)
    }

    private fun showToast(message: String) {
        state.value = AllSystemFragmentState.ShowToast(message)
    }

    fun invokeAllSysAppsUseCase() {
        viewModelScope.launch {
            useCaseSysApps.invokeAllSysAppsUseCase().onStart {
                setLoading()
            }.catch { exception ->
                hideLoading()
                showToast(exception.message.toString())
                exception.printStackTrace()
            }.collect { result ->
                hideLoading()
                when (result) {
                    is DataState.Success -> {
                        _sysAppsLists.value = result.data
                    }

                    else -> {
                        showToast("result.Some thing went Wrong")
                    }
                }
            }
        }
    }
}

sealed class AllSystemFragmentState {
    object Init : AllSystemFragmentState()
    data class IsLoading(val isLoading: Boolean) : AllSystemFragmentState()
    data class ShowToast(val message: String) : AllSystemFragmentState()
}
