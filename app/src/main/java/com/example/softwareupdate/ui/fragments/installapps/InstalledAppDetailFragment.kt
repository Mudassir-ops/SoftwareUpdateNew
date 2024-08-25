package com.example.softwareupdate.ui.fragments.installapps

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.softwareupdate.adapters.allapps.AllAppsEntity
import com.example.softwareupdate.ads.loadNativeAd
import com.example.softwareupdate.ads.populateNativeAdView
import com.example.softwareupdate.databinding.CustomNativeViewBinding
import com.example.softwareupdate.databinding.FragmentInstalledAppDetailBinding
import com.example.softwareupdate.utils.AppConstants
import com.example.softwareupdate.utils.getPackageInfoCompat
import com.example.softwareupdate.utils.isAddedAndNotDetached
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
            ivAppIcon.setImageDrawable(allAppsEntity?.icon)
            tvAppName.text = allAppsEntity?.appName
            tvAppSize.text = "20Mb"
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
            activity?.loadNativeAd { nativeAdLambda ->
                if (this@InstalledAppDetailFragment.isAddedAndNotDetached()) {
                    val adBinding = CustomNativeViewBinding.inflate(layoutInflater)
                    nativeAdLambda?.let { populateNativeAdView(it, adBinding) }
                    nativeAd.removeAllViews()
                    nativeAd.addView(adBinding.root)
                }
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
}