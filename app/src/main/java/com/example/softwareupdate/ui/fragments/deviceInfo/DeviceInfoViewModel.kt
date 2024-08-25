package com.example.softwareupdate.ui.fragments.deviceInfo

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.S)
@HiltViewModel
class DeviceInfoViewModel @Inject constructor(
    deviceInfoModule: DeviceInfoModule
) : ViewModel() {
    private val _deviceInfo = MutableLiveData<DeviceInfo?>()
    val deviceInfo: LiveData<DeviceInfo?> get() = _deviceInfo

    init {
        viewModelScope.launch {
            deviceInfoModule.getDeviceInfo().collect {
                when (it) {
                    is Response.Error -> Log.d("home", "error: ${it.error}")
                    is Response.Success -> {
                        _deviceInfo.value = it.data
                    }
                }
            }
        }
    }

}