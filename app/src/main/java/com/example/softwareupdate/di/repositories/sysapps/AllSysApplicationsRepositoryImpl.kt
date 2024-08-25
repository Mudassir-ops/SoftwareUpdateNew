package com.example.softwareupdate.di.repositories.sysapps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.example.softwareupdate.adapters.allapps.PackageInfoEntity
import com.example.softwareupdate.adapters.sysapps.MySysAppsEntity
import com.example.softwareupdate.utils.DataState
import com.example.softwareupdate.utils.getPackageInfoCompat
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class AllSysApplicationsRepositoryImpl @Inject constructor(
    private val applicationContext: Context,
) : AllSysApplicationsRepository {

    override val allSysAppsList: allSysAppsListFunction = {
        flow {
            val allSysApps: List<PackageInfoEntity?> = getSystemApps(
                context = applicationContext
            )
            val allSysAppsInner = mutableListOf<MySysAppsEntity>()
            allSysApps.forEach { packageInfoEntity ->
                allSysAppsInner.add(
                    MySysAppsEntity(
                        appName = packageInfoEntity?.appName ?: "",
                        pName = packageInfoEntity?.pName ?: "",
                        versionName = packageInfoEntity?.versionName ?: "",
                        versionCode = packageInfoEntity?.versionCode ?: 0,
                        icon = packageInfoEntity?.icon,
                        installationDate = packageInfoEntity?.installationDate ?: "",
                    )
                )
            }
            emit(DataState.Success(allSysAppsInner))
        }
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
            val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return dateFormat.format(Date(installationTime))
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }
}