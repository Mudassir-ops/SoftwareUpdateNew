package com.example.softwareupdate.ui.fragments.installapps

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.softwareupdate.adapters.allapps.AllAppsEntity
import com.example.softwareupdate.databinding.FragmentInstalledAppDetailBinding
import com.example.softwareupdate.utils.AppConstants
import com.example.softwareupdate.utils.getAppSize
import com.example.softwareupdate.utils.getPackageInfoCompat

import com.example.softwareupdate.utils.launchOtherApp
import com.example.softwareupdate.utils.openPlayStoreForApp
import com.example.softwareupdate.utils.parcelable
import com.example.softwareupdate.utils.shareApp


class InstalledAppDetailFragment : Fragment() {
    private val viewModel by viewModels<InstalledAppDetailViewModel>()
    private var _binding: FragmentInstalledAppDetailBinding? = null
    private val binding get() = _binding
    private var allAppsEntity: AllAppsEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        allAppsEntity = arguments?.parcelable(AppConstants.KEY_INSTALL_APP_DETAIL)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInstalledAppDetailBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {

            val appName = allAppsEntity?.appName
            if (appName != null) {
                if (hasUsageStatsPermission(requireContext())) {
                    val packageName = getPackageNameFromAppName(requireContext(), appName)
                    if (packageName != null) {
                        val appSize = getAppSize(requireContext(), packageName)
                        binding?.tvAppSize?.text = appSize
                        Log.d("AppSize", "Size of app $appName ($packageName): $appSize")
                    } else {
                        Log.e("AppSize", "Package not found for app name: $appName")
                    }
                } else {
                    // Request permission if not granted
                    Log.e("AppSize", "Usage stats permission not granted. Requesting permission.")
                    requestUsageStatsPermission(requireContext())
                }
            }

            ivAppIcon.setImageDrawable(allAppsEntity?.icon)
            tvAppName.text = allAppsEntity?.appName
            tvVersion.text = allAppsEntity?.versionName
            tvLastUpdated.text = allAppsEntity?.installationDate
            ivAppIcon.setOnClickListener {
                findNavController().navigateUp()
            }
            btnLaunch.setOnClickListener {
                allAppsEntity?.pName?.let { it1 -> context?.launchOtherApp(packageName = it1) }
            }
            btnViewOnStore.setOnClickListener {
                allAppsEntity?.pName?.let { it1 -> context.openPlayStoreForApp(packageName = it1) }
            }

            checkAppSensitivity(packName = allAppsEntity?.pName ?: return@apply)
            btnShare.setOnClickListener {
                context?.shareApp(allAppsEntity ?: return@setOnClickListener)
            }
        }
    }

    private fun checkAppSensitivity(packName: String) {
        val packageInfo =
            context?.packageManager?.getPackageInfoCompat(packName, PackageManager.GET_PERMISSIONS)
        val permissions = packageInfo?.requestedPermissions

        permissions?.forEach { permission ->
            Log.e("PermissionUsed-->", "checkAppSensitivity: $permission ---- $packName")
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getPackageNameFromAppName(context: Context, appName: String): String? {
        val pm = context.packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        // Iterate through the list of installed apps to find the matching app name
        for (app in installedApps) {
            val label = pm.getApplicationLabel(app).toString()
            if (label == appName) {
                return app.packageName // Return the matching package name
            }
        }
        return null // Return null if the app name doesn't match any package
    }

    private fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
        } else {
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    // Function to request Usage Stats permission by directing user to settings
    private fun requestUsageStatsPermission(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}