package com.example.softwareupdate.utils

import android.app.usage.StorageStats
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.storage.StorageManager
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.File
import java.util.*

fun getAppSize(context: Context, packageName: String): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        getAppSizeOreoAndAbove(context, packageName)
    } else {
        getAppSizeBelowOreo(context, packageName)
    }
}


@RequiresApi(Build.VERSION_CODES.O)
private fun getAppSizeOreoAndAbove(context: Context, packageName: String): String {
    try {
        val storageStatsManager =
            context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

        // Log UUID for debugging
        val storageVolume = storageManager.storageVolumes[0]
        val uuidStr = storageVolume.uuid
        val uuid: UUID = if (uuidStr == null) {
            StorageManager.UUID_DEFAULT
        } else {
            // Log the UUID string
            Log.d("GetAppSize", "UUID String: $uuidStr")
            try {
                UUID.fromString(uuidStr)
            } catch (e: IllegalArgumentException) {
                Log.e("GetAppSize", "Invalid UUID String: $uuidStr", e)
                StorageManager.UUID_DEFAULT
            }
        }

        // Get storage stats for the app
        val appStorageStats = storageStatsManager.queryStatsForPackage(
            uuid,
            packageName,
            android.os.Process.myUserHandle()
        )

        val totalBytes =
            appStorageStats.appBytes + appStorageStats.cacheBytes + appStorageStats.dataBytes
        return formatSize(totalBytes)
    } catch (e: Exception) {
        Log.e("GetAppSize", "Error getting app size: ", e)
        return "Unable to get size"
    }
}

private fun getAppSizeBelowOreo(context: Context, packageName: String): String {
    try {
        val packageManager = context.packageManager
        val appInfo: ApplicationInfo = packageManager.getApplicationInfo(packageName, 0)
        val file = appInfo.sourceDir
        val appFile = File(file)
        val appSize = appFile.length()
        return formatSize(appSize)
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        return "Unable to get size"
    }
}
