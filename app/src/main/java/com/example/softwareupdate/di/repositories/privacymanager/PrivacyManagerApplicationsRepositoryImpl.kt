package com.example.softwareupdate.di.repositories.privacymanager

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.os.Build
import com.example.softwareupdate.adapters.allapps.PackageInfoEntity
import com.example.softwareupdate.adapters.privacymanager.AppPermissions
import com.example.softwareupdate.adapters.privacymanager.PrivacyManagerEntity
import com.example.softwareupdate.utils.DataState
import com.example.softwareupdate.utils.getPackageInfoCompat
import com.example.softwareupdate.utils.getPermissionInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class PrivacyManagerApplicationsRepositoryImpl @Inject constructor(
    private val applicationContext: Context?,
) : PrivacyManagerApplicationsRepository {

    override val allDeviceAppsWithPermissions: AllDeviceAppsFunction = {
        flow {
            val systemApps = getSystemApps(context = applicationContext ?: return@flow)
            val installedApps = getInstalledApps(context = applicationContext)
            val allApps = systemApps + installedApps
            val appWithSensitiveInformation = allApps.map {
                applicationContext.checkAppSensitivity(pkgEntity = it)
            }
            emit(DataState.Success(appWithSensitiveInformation))
        }
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

    private suspend fun getSystemApps(
        context: Context?, dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): List<PackageInfoEntity> = withContext(dispatcher) {
        val res: ArrayList<PackageInfoEntity> = ArrayList()
        val packs: List<PackageInfo?> =
            context?.packageManager?.getInstalledPackages(0) ?: emptyList()
        for (pack in packs) {
            if (isSystemApp(pack)) {
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

    private suspend fun Context?.checkAppSensitivity(
        pkgEntity: PackageInfoEntity?, dispatcher: CoroutineDispatcher = Dispatchers.Default
    ): PrivacyManagerEntity? {
        return withContext(dispatcher) {
            val privacyManagerEntity = PrivacyManagerEntity(
                appName = pkgEntity?.appName,
                pName = pkgEntity?.pName,
                versionName = pkgEntity?.versionName,
                versionCode = pkgEntity?.versionCode ?: 0,
                icon = pkgEntity?.icon,
                installationDate = pkgEntity?.installationDate,
                isSelected = false,
                permissionList = arrayListOf()
            )

            val packageInfo = this@checkAppSensitivity?.packageManager?.getPackageInfoCompat(
                pkgEntity?.pName ?: return@withContext null, PackageManager.GET_PERMISSIONS
            )
            val permissions = packageInfo?.requestedPermissions
            permissions?.let {
                val permissionSensitivity = returnTotalPermissionSensitivity(it, dispatcher)
                privacyManagerEntity.protectionValue = permissionSensitivity.second
                permissionSensitivity.first?.let { it1 ->
                    privacyManagerEntity.permissionList?.addAll(it1)
                }
            }
            return@withContext privacyManagerEntity
        }
    }

    private suspend fun Context?.returnTotalPermissionSensitivity(
        permissions: Array<String>, dispatcher: CoroutineDispatcher = Dispatchers.Default
    ): Pair<ArrayList<AppPermissions>?, Int> {
        return withContext(dispatcher) {
            val appPermissions: ArrayList<AppPermissions> = arrayListOf()
            var dangerousPermissionCount = 0
            permissions.forEach { permission ->
                val permissionInfo = this@returnTotalPermissionSensitivity?.getPermissionInfo(
                    permission
                )
                val protectionLevel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    permissionInfo?.protection
                } else {
                    @Suppress("DEPRECATION") permissionInfo?.protectionLevel
                }
                val isDangerous = if (protectionLevel == PermissionInfo.PROTECTION_DANGEROUS) {
                    dangerousPermissionCount++
                    true
                } else {
                    false
                }
                appPermissions.add(
                    AppPermissions(
                        permissionName = permission,
                        permissionDetail = "",
                        isDangerous = isDangerous
                    )
                )
            }
            return@withContext Pair(appPermissions, dangerousPermissionCount.plus(3))
        }
    }

}