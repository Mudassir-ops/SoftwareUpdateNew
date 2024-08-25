package com.example.softwareupdate.ui.fragments.uninstallapps

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
class AllUnInstalledAppsViewModel @Inject constructor(private val getAllDeviceAppsUseCase: GetAllDeviceAppsUseCase) :
    ViewModel() {

    private val _allUnInstallApps = MutableStateFlow<List<AllAppsEntity?>>(arrayListOf())
    val allUnInstallApps: StateFlow<List<AllAppsEntity?>> get() = _allUnInstallApps

    private val state =
        MutableStateFlow<AllUnInstallAppFragmentState>(AllUnInstallAppFragmentState.Init)
    val mState: StateFlow<AllUnInstallAppFragmentState> get() = state

    private fun setLoading() {
        state.value = AllUnInstallAppFragmentState.IsLoading(true)
    }

    private fun hideLoading() {
        state.value = AllUnInstallAppFragmentState.IsLoading(false)
    }

    private fun showToast(message: String) {
        state.value = AllUnInstallAppFragmentState.ShowToast(message)
    }

    fun invokeAllUnInstallAppsUseCase() {
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
                        if (newData != _allUnInstallApps.value) {
                            _allUnInstallApps.value = newData
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

sealed class AllUnInstallAppFragmentState {
    object Init : AllUnInstallAppFragmentState()
    data class IsLoading(val isLoading: Boolean) : AllUnInstallAppFragmentState()
    data class ShowToast(val message: String) : AllUnInstallAppFragmentState()
}
