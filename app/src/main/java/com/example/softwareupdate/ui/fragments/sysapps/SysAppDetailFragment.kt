package com.example.softwareupdate.ui.fragments.sysapps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.softwareupdate.adapters.allapps.AllAppsEntity
import com.example.softwareupdate.adapters.sysapps.MySysAppsEntity
import com.example.softwareupdate.ads.loadNativeAd
import com.example.softwareupdate.ads.populateNativeAdView
import com.example.softwareupdate.databinding.CustomNativeViewBinding
import com.example.softwareupdate.databinding.FragmentSysAppDetailBinding
import com.example.softwareupdate.utils.AppConstants
import com.example.softwareupdate.utils.invisible
import com.example.softwareupdate.utils.isAddedAndNotDetached
import com.example.softwareupdate.utils.isPlayStoreAvailableForApp
import com.example.softwareupdate.utils.launchOtherApp
import com.example.softwareupdate.utils.openPlayStoreForApp
import com.example.softwareupdate.utils.parcelable
import com.example.softwareupdate.utils.shareApp
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SysAppDetailFragment : Fragment() {

    private val viewModel by viewModels<SysAppDetailViewModel>()
    private var _binding: FragmentSysAppDetailBinding? = null
    private val binding get() = _binding
    private var mySysAppsEntity: MySysAppsEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mySysAppsEntity = arguments?.parcelable(AppConstants.SYSTEM_APPS_PACKAGE)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSysAppDetailBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding?.apply {
            ivAppIcon.setImageDrawable(mySysAppsEntity?.icon)
            tvAppName.text = mySysAppsEntity?.appName
            tvAppSize.text = "20Mb"
            tvVersion.text = mySysAppsEntity?.versionName
            tvLastUpdated.text = mySysAppsEntity?.installationDate
            ivAppIcon.setOnClickListener {
                findNavController().navigateUp()
            }
            btnLaunch.setOnClickListener {
                mySysAppsEntity?.pName?.let { it1 -> context?.launchOtherApp(packageName = it1) }
            }
            btnViewOnStore.setOnClickListener {

                mySysAppsEntity?.pName?.let { it1 -> context.openPlayStoreForApp(packageName = it1) }
            }

            activity?.loadNativeAd { nativeAdLambda ->
                if (this@SysAppDetailFragment.isAddedAndNotDetached()) {
                    val adBinding = CustomNativeViewBinding.inflate(layoutInflater)
                    nativeAdLambda?.let { populateNativeAdView(it, adBinding) }
                    nativeAd.removeAllViews()
                    nativeAd.addView(adBinding.root)
                }
            }
            btnShare.setOnClickListener {
                context?.shareApp(
                    AllAppsEntity(
                        appName = mySysAppsEntity?.appName ?: "",
                        pName = mySysAppsEntity?.pName ?: "",
                        versionName = mySysAppsEntity?.versionName ?: "",
                        versionCode = mySysAppsEntity?.versionCode ?: 0,
                        icon = mySysAppsEntity?.icon,
                        installationDate = mySysAppsEntity?.installationDate ?: "",
                    )
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}