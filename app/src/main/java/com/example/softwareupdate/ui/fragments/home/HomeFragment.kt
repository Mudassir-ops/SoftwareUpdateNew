package com.example.softwareupdate.ui.fragments.home

import android.Manifest
import android.app.AlertDialog
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.softwareupdate.R
import com.example.softwareupdate.RateUsDialog
import com.example.softwareupdate.adapters.home.HomeScreensItemsAdapter
import com.example.softwareupdate.databinding.FragmentHomeBinding
import com.example.softwareupdate.dialog.AppUsageAccess
import com.example.softwareupdate.dialog.ExitDialog
import com.example.softwareupdate.service.CheckSoftwareService
import com.example.softwareupdate.service.CheckSoftwareService.Companion.isRunning
import com.example.softwareupdate.ui.fragments.appPrivacy.AppPrivacyRiskManagerViewModel
import com.example.softwareupdate.ui.fragments.sysapps.AllSystemAppsViewModel
import com.example.softwareupdate.utils.ActionType
import com.example.softwareupdate.utils.AppConstants.IF_FIRST_TIME_OPEN_APP
import com.example.softwareupdate.utils.all_extension.gone
import com.example.softwareupdate.utils.all_extension.moreApps
import com.example.softwareupdate.utils.all_extension.privacyPolicyUrl
import com.example.softwareupdate.utils.all_extension.rateUs
import com.example.softwareupdate.utils.all_extension.shareApp
import com.example.softwareupdate.utils.all_extension.showToast
import com.example.softwareupdate.utils.all_extension.toast
import com.example.softwareupdate.utils.calculateProgress
import com.example.softwareupdate.utils.event.UpdateEvent
import com.example.softwareupdate.utils.feedBackWithEmail
import com.example.softwareupdate.utils.handleSystemUpdate
import com.example.softwareupdate.utils.initDrawerClicks
import com.example.softwareupdate.utils.initHomeItemsData
import com.example.softwareupdate.utils.isInternetAvailable
import com.example.softwareupdate.utils.isServiceRunning
import com.example.softwareupdate.utils.setGradientTextShader
import com.example.softwareupdate.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val viewModel by viewModels<HomeViewModel>()
    private val sharedViewModel: AppPrivacyRiskManagerViewModel by activityViewModels()
    private val sharedViewModelSystemApps: AllSystemAppsViewModel by activityViewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding
    private var homeScreensItemsAdapter: HomeScreensItemsAdapter? = null
    private var isBtnStart = false
    private var exitDialog: AlertDialog? = null
    private var rateUsDialog: RateUsDialog? = null
    private var exitDialogNew: ExitDialog? = null
    private var appUsageAccessDialog: AppUsageAccess? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appUsageAccessDialog = AppUsageAccess(requireActivity(), callback = { accessGranted ->
            if (accessGranted) {
                if (isUsageAccessGranted()) {
                    activity?.showToast("Permission granted. Proceeding with functionality.")
                } else {
                    // Permission not granted, guide user to settings
                    activity?.showToast("Permission not granted. Please enable it in settings.")
                }
            } else {
                // Handle case when user denies access
                activity?.showToast("Permission denied.")
            }
        })
        sharedViewModel.invokePrivacyManagerAppsUseCase()
        sharedViewModelSystemApps.invokeAllSysAppsUseCase()
        rateUsDialog = RateUsDialog(activity ?: return)
        exitDialogNew = ExitDialog(activity = activity ?: return)

        homeScreensItemsAdapter = HomeScreensItemsAdapter(
            callback = { actionType: ActionType ->
                when (actionType) {
                    ActionType.ACTION_APP_PRIVACY -> if (findNavController().currentDestination?.id == R.id.navigation_home) {
                        findNavController().navigate(R.id.action_navigation_home_to_app_privacy_manager)
                    }

                    ActionType.ACTION_APP_USAGE -> {
                        if (isUsageAccessGranted()) {
                            if (findNavController().currentDestination?.id == R.id.navigation_home) {
                                findNavController().navigate(R.id.action_navigation_home_to_navigation_updatedApps)
                            }
                        } else
                            appUsageAccessDialog?.show()


                    }

                    ActionType.ACTION_APP_UNINSTALL -> if (findNavController().currentDestination?.id == R.id.navigation_home) {
                        findNavController().navigate(R.id.action_navigation_home_to_navigation_all_UnInstalled_apps)
                    }

                    ActionType.ACTION_APP_INSTALL -> if (findNavController().currentDestination?.id == R.id.navigation_home) {
                        findNavController().navigate(R.id.action_navigation_home_to_navigation_all_installed_apps)
                    }
                }

            }, dataList = context?.initHomeItemsData() ?: emptyList()
        )

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback = object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
//                if (!isDialogVisible()) {
//                    showExitDialog()
//                }
                exitDialogNew?.show()
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(this, callback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.headerLayout?.btnPremium?.visibility = View.INVISIBLE
        binding?.apply {
            setUpButtonViewMore()
            setUpButtonSystemUpdate()
            setUpButtonAndroidVersions()
            setUpButtonSystemApplications()
            setUpStartButton()
            setUpHeaderLayout()
            initDrawerClicks(
                colorString = "#F21100"
            ) { clickedViewIndex ->
                handleDrawerClick(clickedViewIndex)
            }
            setUpRecyclerView()
            viewModel.getAllUpdatedApp()
            viewModel.getRowCount()
            observeAppCount()
            if (IF_FIRST_TIME_OPEN_APP) {
                binding?.tvBtnStart?.setGradientTextShader(
                    context ?: return, resources.getString(R.string.tap_to_start_scanning)
                )
            } else {
                binding?.tvBtnStart?.setGradientTextShader(
                    context ?: return, resources.getString(R.string.start_nscan)
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding?.mainDrawerLayout?.let {
            if (it.isDrawerOpen(GravityCompat.START)) {
                it.closeDrawer(GravityCompat.START)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        exitDialog?.let { dialog ->
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
    }

    private fun FragmentHomeBinding.setUpButtonViewMore() {
        btnViewMore.setOnClickListener {
            if (tvUpdateAppCountStatic.text.toString().trim() != "0") {
                navigateToUpdatedApps()
            } else {
                resetProgressBarAndSetText()
                startCheckSoftwareService()
            }
        }
    }

    private fun FragmentHomeBinding.setUpButtonSystemUpdate() {
        btnSystemUpdate.setOnClickListener {
            activity?.handleSystemUpdate()
        }
    }

    private fun FragmentHomeBinding.setUpButtonAndroidVersions() {
        btnAndroidVersions.setOnClickListener {
            navigateToApiVersions()
        }
    }

    private fun FragmentHomeBinding.setUpButtonSystemApplications() {
        btnSystemApplications.setOnClickListener {
            navigateToSystemApplications()
        }
    }

    private fun FragmentHomeBinding.setUpStartButton() {
        btnStart.setOnClickListener {
            if (context?.isInternetAvailable() == true) {
                if (isRunning) {
                    activity?.toast("Already Checking")
                    return@setOnClickListener
                }
                resetProgressBarAndSetText()
                startCheckSoftwareService()
            } else {
                activity?.toast(getString(R.string.no_internet))
            }

        }
    }

    private fun FragmentHomeBinding.setUpHeaderLayout() {
        headerLayout.logoBg.setOnClickListener {
            mainDrawerLayout.openDrawer(GravityCompat.START)
        }
        headerLayout.btnPremium.setOnClickListener {
            if (context?.isInternetAvailable() == true) {
                context?.showToast(getString(R.string.coming_soon))
            } else {
                context?.showToast(getString(R.string.no_internet))
            }
        }
    }

    private fun navigateToUpdatedApps() {
        if (findNavController().currentDestination?.id == R.id.navigation_home) {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_updatedApps)
        }
    }

    private fun FragmentHomeBinding.resetProgressBarAndSetText() {
        progressBar.progress = 0
        tvBtnStart.setGradientTextShader(context ?: return, resources.getString(R.string.scanning))
    }

    private fun startCheckSoftwareService() {
        if (!isServiceRunning(context ?: return, CheckSoftwareService::class.java)) {
            if (isRunning) {
                activity?.toast("Already Checking")
                return
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        context ?: return, Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    binding?.btnViewMore?.gone()
                    ContextCompat.startForegroundService(
                        context ?: return,
                        Intent(context ?: return, CheckSoftwareService::class.java)
                    )
                }
            } else {
                binding?.btnViewMore?.gone()
                ContextCompat.startForegroundService(
                    context ?: return,
                    Intent(context ?: return, CheckSoftwareService::class.java)
                )
            }
        }
    }

    private fun navigateToApiVersions() {
        if (findNavController().currentDestination?.id == R.id.navigation_home) {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_api_versions)
        }
    }

    private fun navigateToSystemApplications() {
        if (findNavController().currentDestination?.id == R.id.navigation_home) {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_system_applications)
        }
    }

    private fun FragmentHomeBinding?.observeAppCount() {
        viewModel.appCount.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { appCount ->
                if (appCount > 0) {
                    this?.btnViewMore?.show()
                }
                this?.tvUpdateAppCountStatic.let { it?.text = appCount.toString() }
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun FragmentHomeBinding?.handleDrawerClick(clickedViewIndex: Int) {
        this?.mainDrawerLayout?.closeDrawer(GravityCompat.START)
        when (clickedViewIndex) {
            0 -> this?.mainDrawerLayout?.closeDrawer(GravityCompat.START)
            1 -> navigateToDeviceInfo()
            2 -> activity?.privacyPolicyUrl()
            3 -> activity?.shareApp()
            4 -> activity?.moreApps()
            5 -> activity?.rateUs()
            6 -> context?.feedBackWithEmail("Feedback", "Any Feedback", "shabirehtisham8@gmail.com")
        }
    }

    private fun navigateToDeviceInfo() {
        if (findNavController().currentDestination?.id == R.id.navigation_home) {
            findNavController().navigate(R.id.action_navigation_home_to_deviceInfoFragment)
        }
    }

    private fun FragmentHomeBinding.setUpRecyclerView() {
        rvHomeItems.adapter = homeScreensItemsAdapter
        rvHomeItems.hasFixedSize()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this@HomeFragment)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this@HomeFragment)
        super.onStop()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateEvent(event: UpdateEvent) {
        if (!event.isAllAppCheckFinished) {
            val textSizeInSp = resources.getDimension(com.intuit.ssp.R.dimen._8ssp)
            binding?.tvBtnStart?.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInSp)
            handleAppCheckInProgress(event)
        } else {
            val textSizeInSp = resources.getDimension(com.intuit.ssp.R.dimen._8ssp)
            binding?.tvBtnStart?.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInSp)
            handleAllAppCheckFinished(event)
        }
    }

    private fun handleAppCheckInProgress(event: UpdateEvent) {
        if (binding?.tvBtnStart?.text?.toString() == resources.getString(R.string.start_nscan)) {
            binding?.tvBtnStart?.setGradientTextShader(
                context ?: return, resources.getString(R.string.scanning)
            )
        }
        val progress = calculateProgress(event.currentAppCheckIn, event.totalAppSize)
        updateUIInProgress(progress, event.count)
    }

    private fun handleAllAppCheckFinished(event: UpdateEvent) {
        if (binding?.tvBtnStart?.text?.toString() == resources.getString(R.string.start_nscan)) {
            binding?.tvBtnStart?.setGradientTextShader(
                context ?: return, resources.getString(R.string.scanning)
            )
        }
        updateUIAfterCompletion(event.count)
    }

    private fun updateUIInProgress(progress: Int, count: Int) {
        binding?.progressBar?.progress = progress + 1
        binding?.tvUpdateAppCountStatic?.text = count.toString()
        if (progress == 99) {
            binding?.tvBtnStart?.setGradientTextShader(
                context ?: return,
                resources.getString(R.string.scanned)
            )
            isBtnStart = true
        } else {
            binding?.tvBtnStart?.setGradientTextShader(
                context ?: return,
                "$progress %"
            )
        }
    }

    private fun updateUIAfterCompletion(count: Int) {
        binding?.progressBar?.progress = 100
        binding?.tvUpdateAppCountStatic?.text = count.toString()
        binding?.tvBtnStart?.setGradientTextShader(
            context ?: return,
            resources.getString(R.string.scanned)
        )
        binding?.btnViewMore?.visibility = View.VISIBLE
        isBtnStart = true
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCheckSoftwareService()
        } else {
            context?.showToast(getString(R.string.permission_denied))
        }
    }

    private fun isUsageAccessGranted(): Boolean {
        val appOpsManager = context?.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
        val applicationInfo = context?.applicationInfo
        val mode =
            appOpsManager?.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                applicationInfo?.uid ?: return false,
                context?.packageName ?: return false
            )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}


