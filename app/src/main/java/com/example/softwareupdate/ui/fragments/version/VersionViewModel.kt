package com.example.softwareupdate.ui.fragments.version

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.softwareupdate.adapters.version.VersionsEntity
import com.example.softwareupdate.di.usecase.GetAllAndroidVersionsUseCase
import com.example.softwareupdate.utils.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class VersionViewModel @Inject constructor(private val getAllAndroidVersionsUseCase: GetAllAndroidVersionsUseCase) :
    ViewModel() {

    private val _versionLists = MutableStateFlow<List<VersionsEntity?>>(arrayListOf())
    val versionList: StateFlow<List<VersionsEntity?>> get() = _versionLists

    private val state = MutableStateFlow<VersionFragmentState>(VersionFragmentState.Init)
    val mState: StateFlow<VersionFragmentState> get() = state

    private fun setLoading() {
        state.value = VersionFragmentState.IsLoading(true)
    }

    private fun hideLoading() {
        state.value = VersionFragmentState.IsLoading(false)
    }

    private fun showToast(message: String) {
        state.value = VersionFragmentState.ShowToast(message)
    }


    fun getAllVersions(context: Context?) {
        viewModelScope.launch {
            getAllAndroidVersionsUseCase.invokeAllAppVersionsUseCase(context).onStart {
                setLoading()
            }.catch { exception ->
                hideLoading()
                showToast(exception.message.toString())
                exception.printStackTrace()
            }.collect { result ->
                hideLoading()
                when (result) {
                    is DataState.Success -> {
                        _versionLists.value = result.data
                    }

                    else -> {
                        showToast("result.Some thing went Wrong")
                    }
                }
            }
        }
    }
}

sealed class VersionFragmentState {
    object Init : VersionFragmentState()
    data class IsLoading(val isLoading: Boolean) : VersionFragmentState()
    data class ShowToast(val message: String) : VersionFragmentState()
}
