package com.example.softwareupdate.di.repositories.checkupdaterepository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import com.example.softwareupdate.adapters.allapps.PackageInfoEntity
import com.example.softwareupdate.data.UpdatedAppDao
import com.example.softwareupdate.utils.getPackageInfoCompat
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class CheckUpdateRepositoryImpl @Inject constructor(
    private val applicationContext: Context?,
    private val updatedAppDao: UpdatedAppDao
) : CheckUpdateRepository {

    override suspend fun checkAppUpdateCount(
        callback: (Int, String, Int, Int, PackageInfoEntity) -> Unit,
        callbackFinished: (Boolean) -> Unit
    ): Int {
        val systemApps = getSystemApps(context = applicationContext ?: return 0)
        val installedApps = getInstalledApps(context = applicationContext)
        val allApps = systemApps + installedApps
        var updatedAppCount = 0
        var currentAppCount = 0
        allApps.forEach { app ->
            val packageName = app.pName
            val checkUpdate = checkUpdates(packageName)
            currentAppCount++
            Log.e("IsAppUpdateRequired--->", "checkAppUpdate: $checkUpdate")
            if (checkUpdate) {
                updatedAppCount++
                callback.invoke(updatedAppCount, app.appName, currentAppCount, allApps.size, app)
            }
            if (currentAppCount == allApps.size) {
                callback.invoke(updatedAppCount, app.appName, currentAppCount, allApps.size, app)
                callbackFinished.invoke(true)
            }
        }
        return updatedAppCount
    }

    private suspend fun checkUpdates(
        packageName: String,
    ): Boolean {
        return withContext(IO) {
            try {
                val url = "https://play.google.com/store/apps/details?id=$packageName"
                val document: Document? = Jsoup.connect(url).get()
                val lastUpdatedElementPlayStore = document?.select("div.xg1aie")?.first()?.text()
                val packageInfo =
                    applicationContext?.packageManager.getPackageInfoCompat(packageName)
                val lastUpdatedLocal = convertMillisToDateString(packageInfo?.lastUpdateTime)
                val comparison = if (lastUpdatedElementPlayStore != null) {
                    compareDates(lastUpdatedElementPlayStore, lastUpdatedLocal)
                } else {
                    null
                }
                comparison == true
            } catch (e: Exception) {
                Log.e("Error", "An error occurred: ${e.message}", e)
                false
            }
        }
    }

    private fun compareDates(date1: String, date2: String): Boolean {
        try {
            val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH)
            val cal1 = Calendar.getInstance()
            val cal2 = Calendar.getInstance()
            cal1.time = dateFormat.parse(date1) ?: return false
            cal2.time = dateFormat.parse(date2) ?: return false
            val result = cal1.compareTo(cal2)
            return if (result > 0) {
                true
            } else if (result < 0) {
                false
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun convertMillisToDateString(millis: Long?): String {
        if (millis == null) {
            return ""
        }
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH)
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        return dateFormat.format(cal.time)
    }


    private suspend fun getSystemApps(
        context: Context?, dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): List<PackageInfoEntity> = withContext(dispatcher) {
        val res: ArrayList<PackageInfoEntity> = ArrayList()
        val packs: List<PackageInfo?> =
            context?.packageManager?.getInstalledPackages(0) ?: emptyList()
        for (pack in packs) {
            if (isSystemApp(pack) && isUpdatedSystemApp(pack)) {
                val newInfo = createPackageInfoEntity(context, pack)
                if (newInfo.icon != null) {
                    res.add(newInfo)
                }
            }
        }
        res
    }

    private suspend fun getInstalledApps(
        context: Context?, dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): List<PackageInfoEntity> = withContext(dispatcher) {
        val res: ArrayList<PackageInfoEntity> = ArrayList()
        val packs: List<PackageInfo?> =
            context?.packageManager?.getInstalledPackages(0) ?: emptyList()
        for (pack in packs) {
            if (pack != null && (pack.applicationInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM)) == 0) {
                val newInfo = createPackageInfoEntity(context, pack)
                if (newInfo.icon != null) {
                    res.add(newInfo)
                }
            }
        }
        return@withContext res
    }

    private fun isSystemApp(pack: PackageInfo?): Boolean =
        (pack?.applicationInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM)) != 0

    private fun isUpdatedSystemApp(pack: PackageInfo?): Boolean =
        (pack?.applicationInfo?.flags?.and(ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) ?: false) != 0

    private fun createPackageInfoEntity(context: Context?, pack: PackageInfo?): PackageInfoEntity {
        return PackageInfoEntity(appName = context?.packageManager?.let {
            pack?.applicationInfo?.loadLabel(it)?.toString()
        } ?: "",
            pName = pack?.packageName ?: "",
            versionName = pack?.versionName ?: "",
            versionCode = 0,
            icon = pack?.applicationInfo?.loadIcon(context?.packageManager),
            installationDate = getInstallationDate(context, pack?.packageName ?: ""))
    }

    private fun getInstallationDate(context: Context?, packageName: String): String {
        try {
            val packageInfo = context?.packageManager?.getPackageInfoCompat(packageName)
            val installationTime = packageInfo?.lastUpdateTime ?: 0
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return dateFormat.format(Date(installationTime))
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }

    override suspend fun insertUpdatedApp(packageInfoEntity: com.example.softwareupdate.data.PackageInfoEntity) {
        updatedAppDao.insertUpdatedApp(packageInfoEntity)
    }

    override fun getAllUpdatedApp(): Flow<List<com.example.softwareupdate.data.PackageInfoEntity>> {
        return updatedAppDao.getAllUpdatedApp()
    }

    override suspend fun getRowCount(): Int {
        return updatedAppDao.getRowCount()
    }

    override suspend fun deleteAllTables() {
        updatedAppDao.deleteAllTables()
    }
}