package com.example.softwareupdate.di.repositories.allapps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.example.softwareupdate.adapters.allapps.AllAppsEntity
import com.example.softwareupdate.adapters.allapps.PackageInfoEntity
import com.example.softwareupdate.utils.DataState
import com.example.softwareupdate.utils.getPackageInfoCompat
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class AllDeviceApplicationsRepositoryImpl @Inject constructor(
    private val applicationContext: Context,
) : AllDeviceApplicationsRepository {

    override val allDeviceApps: AllDeviceAppsFunction = {
        flow {
            val allSysApps: List<PackageInfoEntity?> =
                getInstalledApps(context = applicationContext)
            val allSysAppsInner = mutableListOf<AllAppsEntity>()
            allSysApps.forEach { packageInfoEntity ->
                allSysAppsInner.add(
                    AllAppsEntity(
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


    private suspend fun getInstalledApps(
        context: Context?,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
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

    private fun createPackageInfoEntity(context: Context?, pack: PackageInfo?): PackageInfoEntity {
        return PackageInfoEntity(
            appName = context?.packageManager?.let {
                pack?.applicationInfo?.loadLabel(it)?.toString()
            } ?: "",
            pName = pack?.packageName ?: "",
            versionName = pack?.versionName ?: "",
            versionCode = 0,
            icon = pack?.applicationInfo?.loadIcon(context?.packageManager),
            installationDate = getInstallationDate(context, pack?.packageName ?: "")
        )
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