package com.example.softwareupdate.ui.fragments.appPrivacy

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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.softwareupdate.adapters.privacymanager.PrivacyManagerDetailItemsAdapter
import com.example.softwareupdate.adapters.privacymanager.PrivacyManagerEntity
import com.example.softwareupdate.databinding.FragmentPrivacyManagerDetailBinding
import com.example.softwareupdate.utils.AppConstants.PRIVACY_MANGER_OBJECT
import com.example.softwareupdate.utils.all_extension.gone
import com.example.softwareupdate.utils.getAppSize
import com.example.softwareupdate.utils.parcelable

class PrivacyManagerDetailFragment : Fragment() {

    private var _binding: FragmentPrivacyManagerDetailBinding? = null
    private val binding get() = _binding
    private var privacyManagerEntity: PrivacyManagerEntity? = null
    private var privacyManagerDetailItemsAdapter: PrivacyManagerDetailItemsAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        privacyManagerEntity = arguments?.parcelable(PRIVACY_MANGER_OBJECT)
        privacyManagerDetailItemsAdapter =
            PrivacyManagerDetailItemsAdapter(callback = {}, dataList = arrayListOf())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPrivacyManagerDetailBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appName = privacyManagerEntity?.appName
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

        binding?.apply {
            headerLayout.btnHeaderBackArrow.setOnClickListener {
                findNavController().navigateUp()
            }
            ivAppIcon.setImageDrawable(privacyManagerEntity?.icon)
            tvAppName.text = privacyManagerEntity?.appName

            headerLayout.tvHeaderVersion.gone()
        }
        setupVersionsRecyclerView()
        handleData()
    }

    private fun setupVersionsRecyclerView() {
        binding?.rvPrivacyManagerDetail?.apply {
            adapter = privacyManagerDetailItemsAdapter
            hasFixedSize()
        }
    }

    private fun handleData() {
        binding?.rvPrivacyManagerDetail?.adapter?.let {
            if (it is PrivacyManagerDetailItemsAdapter) {
                privacyManagerEntity?.let { it1 ->
                    it1.permissionList?.let { it2 ->
                        it.updateVersionsList(
                            it2
                        )
                    }
                }
            }
        }


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