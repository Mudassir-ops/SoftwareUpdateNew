package com.example.softwareupdate.ui.fragments.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.softwareupdate.data.PackageInfoEntity
import com.example.softwareupdate.di.usecase.CheckAppUpdateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(private val updateUseCase: CheckAppUpdateUseCase) :
    ViewModel() {

    private val _appLists = MutableStateFlow<List<PackageInfoEntity?>>(arrayListOf())
    val appsLists: StateFlow<List<PackageInfoEntity?>> get() = _appLists

    private val _appCount = MutableStateFlow(0)
    val appCount: StateFlow<Int> get() = _appCount

    private val state = MutableStateFlow<UpdatedAppFragmentState>(UpdatedAppFragmentState.Init)
    val mState: StateFlow<UpdatedAppFragmentState> get() = state

    private fun setLoading() {
        state.value = UpdatedAppFragmentState.IsLoading(true)
    }

    private fun hideLoading() {
        state.value = UpdatedAppFragmentState.IsLoading(false)
    }

    private fun showToast(message: String) {
        state.value = UpdatedAppFragmentState.ShowToast(message)
    }

    fun getAllUpdatedApp() {
        viewModelScope.launch {
            updateUseCase.getAllUpdatedApp().onStart {
                setLoading()
            }.catch { exception ->
                hideLoading()
                showToast(exception.message.toString())
                exception.printStackTrace()
            }.collect { result ->
                hideLoading()
                if (result.isNotEmpty()) {
                    if (result != _appLists.value) {
                        _appLists.value = result
                    }
                }
            }
        }
    }

    fun getRowCount() {
        viewModelScope.launch {
            withContext(IO) {
                Log.d("getRowCount", "getRowCount: ${updateUseCase.getRowCount()}")
                _appCount.value = updateUseCase.getRowCount()
            }
        }
    }

}

sealed class UpdatedAppFragmentState {
    object Init : UpdatedAppFragmentState()
    data class IsLoading(val isLoading: Boolean) : UpdatedAppFragmentState()
    data class ShowToast(val message: String) : UpdatedAppFragmentState()
}
