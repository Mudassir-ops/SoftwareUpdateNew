package com.example.softwareupdate.ui.fragments.deviceInfo

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.example.softwareupdate.utils.formatSize
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DeviceInfoModule @Inject constructor(@ApplicationContext private val context: Context) {

    @SuppressLint("HardwareIds")
    fun getDeviceInfo() : Flow<Response<DeviceInfo>> = flow{

        val response = try {
            val deviceName = Build.DEVICE
            val deviceModel = Build.MODEL
            val deviceBrand = Build.MANUFACTURER
            val deviceVersion = Build.VERSION.RELEASE

            val statFs = StatFs(Environment.getDataDirectory().path)
            val totalStorage = statFs.blockCountLong * statFs.blockSizeLong
            val availableStorage = statFs.availableBlocksLong * statFs.blockSizeLong

            val totalStorageFormatted = formatSize(totalStorage)
            val availableStorageFormatted = formatSize(availableStorage)

            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)

            val totalRam = memoryInfo.totalMem
            val availableRam = memoryInfo.availMem

            // Format RAM sizes to human-readable form
            val totalRamFormatted = formatSize(totalRam)
            val availableRamFormatted = formatSize(availableRam)

            val softwareId: String =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Settings.Secure.getString(
                        context.contentResolver,
                        Settings.Secure.ANDROID_ID
                    )
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {  // API 30
                    val manager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                    manager.enrollmentSpecificId
                } else {
                    "N/A"  // Fallback for older devices
                }

            val info = DeviceInfo(
                deviceName = deviceName,
                deviceModel = deviceModel,
                deviceRam = totalRamFormatted,
                softwareId = softwareId,
                deviceVersion = deviceVersion.toString(),
                availableStorage = totalStorageFormatted,
                deviceBrand = deviceBrand
            )

            Response.Success(info)

        } catch (e: Exception) {
            Response.Error(e.message ?: "Unexpected error occurred")
        }

        emit(response)

    }
}