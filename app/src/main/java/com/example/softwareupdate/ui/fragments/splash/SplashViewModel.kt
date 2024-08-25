package com.example.softwareupdate.ui.fragments.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.softwareupdate.di.usecase.CheckAppUpdateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(private val updateUseCase: CheckAppUpdateUseCase) :
    ViewModel() {

    fun deleteAllTables(ioDispatcher: CoroutineDispatcher?) {
        viewModelScope.launch(ioDispatcher ?: return) {
            updateUseCase.deleteAllTables()
        }
    }
}