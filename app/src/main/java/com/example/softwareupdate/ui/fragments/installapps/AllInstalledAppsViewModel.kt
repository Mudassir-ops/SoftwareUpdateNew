package com.example.softwareupdate.ui.fragments.installapps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.softwareupdate.adapters.allapps.AllAppsEntity
import com.example.softwareupdate.di.usecase.GetAllDeviceAppsUseCase
import com.example.softwareupdate.utils.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllInstalledAppsViewModel @Inject constructor(private val getAllDeviceAppsUseCase: GetAllDeviceAppsUseCase) :
    ViewModel() {

    private val _sysAppsLists = MutableStateFlow<List<AllAppsEntity?>>(arrayListOf())
    val sysAppsLists: StateFlow<List<AllAppsEntity?>> get() = _sysAppsLists

    private val state =
        MutableStateFlow<AllInstallAppFragmentState>(AllInstallAppFragmentState.Init)
    val mState: StateFlow<AllInstallAppFragmentState> get() = state

    private fun setLoading() {
        state.value = AllInstallAppFragmentState.IsLoading(true)
    }

    private fun hideLoading() {
        state.value = AllInstallAppFragmentState.IsLoading(false)
    }

    private fun showToast(message: String) {
        state.value = AllInstallAppFragmentState.ShowToast(message)
    }

    fun invokeAllDeviceAppsUseCase() {
        viewModelScope.launch {
            getAllDeviceAppsUseCase.invokeAllDeviceAppsUseCase().onStart {
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
                        if (newData != _sysAppsLists.value) {
                            _sysAppsLists.value = newData
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

sealed class AllInstallAppFragmentState {
    object Init : AllInstallAppFragmentState()
    data class IsLoading(val isLoading: Boolean) : AllInstallAppFragmentState()
    data class ShowToast(val message: String) : AllInstallAppFragmentState()
}
