package com.example.softwareupdate.di.repositories.checkupdaterepository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import com.example.softwareupdate.adapters.allapps.PackageInfoEntity
import com.example.softwareupdate.data.UpdatedAppDao
import com.example.softwareupdate.utils.getPackageInfoCompat
import com.google.gson.Gson
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
            val checkUpdate = checkUpdates(packageName, app)
            currentAppCount++
            if (checkUpdate.first == true) {
                updatedAppCount++
                callback.invoke(
                    updatedAppCount,
                    checkUpdate.third.appName,
                    currentAppCount,
                    allApps.size,
                    checkUpdate.third
                )
            }
            if (currentAppCount == allApps.size) {
                callback.invoke(
                    updatedAppCount,
                    checkUpdate.third.appName,
                    currentAppCount,
                    allApps.size,
                    checkUpdate.third
                )
                callbackFinished.invoke(true)
            }
        }
        return updatedAppCount
    }

    private suspend fun checkUpdates(
        packageName: String,
        app: PackageInfoEntity,
    ): Triple<Boolean?, String, PackageInfoEntity> {
        return withContext(IO) {
            try {
                val url = "https://play.google.com/store/apps/details?id=$packageName"
                val document: Document? = Jsoup.connect(url).get()
                val lastUpdatedElementPlayStore = document?.select("div.xg1aie")?.first()?.text()
                val packageInfo =
                    applicationContext?.packageManager.getPackageInfoCompat(packageName)
                val lastUpdatedLocal = convertMillisToDateString(packageInfo?.lastUpdateTime)
                val comparison = if (lastUpdatedElementPlayStore != null) {
                    compareDates(lastUpdatedElementPlayStore, lastUpdatedLocal, packageName)
                } else {
                    null
                }
                Triple(comparison == true, packageName, app)
            } catch (e: Exception) {
                Log.e("Error", "An error occurred: ${e.message}", e)
                Triple(false, packageName, app)
            }
        }
    }

    private fun compareDates(date1: String?, date2: String?, packageName: String): Boolean {
        if (date1.isNullOrBlank() || date2.isNullOrBlank()) return false
        val dateFormats = listOf(
            "MMM d, yyyy",
            "yyyy-MM-dd",
            "dd/MM/yyyy",
            "MM/dd/yyyy",
            "dd MMM yyyy"
        )
        try {
            val parsedDate1 = parseDateWithFormats(date1, dateFormats)
            val parsedDate2 = parseDateWithFormats(date2, dateFormats)
            if (parsedDate1 == null || parsedDate2 == null) return false
            val dateComparison = parsedDate1.after(parsedDate2)
            Log.e(
                "compareDatesCouncil",
                "compareDates: ${parsedDate1}----${parsedDate2}-------${packageName}---${dateComparison}"
            )
            return parsedDate1.after(parsedDate2)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

//    private fun compareDates(date1: String, date2: String): Boolean {
//        try {
//            val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
//            val cal1 = Calendar.getInstance()
//            val cal2 = Calendar.getInstance()
//            cal1.time = dateFormat.parse(date1) ?: return false
//            cal2.time = dateFormat.parse(date2) ?: return false
//            val result = cal1.compareTo(cal2)
//            return if (result > 0) {
//                true
//            } else if (result < 0) {
//                false
//            } else {
//                false
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            return false
//        }
//    }

    private fun parseDateWithFormats(date: String, formats: List<String>): Date? {
        for (format in formats) {
            try {
                val dateFormat = SimpleDateFormat(format, Locale.getDefault())
                return dateFormat.parse(date)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
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

    private fun isUpdateRequired(localVersion: String?, playStoreVersion: String?): Boolean {
        if (localVersion == null || playStoreVersion == null) return false
        val localParts = localVersion.split(".").map { it.toIntOrNull() ?: 0 }
        val playStoreParts = playStoreVersion.split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(localParts.size, playStoreParts.size)) {
            val localPart = localParts.getOrElse(i) { 0 }
            val playStorePart = playStoreParts.getOrElse(i) { 0 }
            if (localPart < playStorePart) return true
            if (localPart > playStorePart) return false
        }
        return false // Versions are identical
    }

    private fun getLocalAppVersion(packageName: String): String? {
        return try {
            val packageInfo = applicationContext?.packageManager?.getPackageInfo(packageName, 0)
            packageInfo?.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("Error", "App not found: ${e.message}", e)
            null
        }
    }

}