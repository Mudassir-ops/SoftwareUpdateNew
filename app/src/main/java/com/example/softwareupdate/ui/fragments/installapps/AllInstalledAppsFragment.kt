package com.example.softwareupdate.ui.fragments.installapps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.softwareupdate.R
import com.example.softwareupdate.adapters.allapps.AllAppsEntity
import com.example.softwareupdate.adapters.allapps.installapps.AllInstallAppsItemsAdapter
import com.example.softwareupdate.databinding.FragmentAllInstalledAppsBinding
import com.example.softwareupdate.utils.AppConstants
import com.example.softwareupdate.utils.all_extension.gone
import com.example.softwareupdate.utils.all_extension.showToast
import com.example.softwareupdate.utils.all_extension.toast
import com.example.softwareupdate.utils.all_extension.visible
import com.example.softwareupdate.utils.isInternetAvailable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class AllInstalledAppsFragment : Fragment() {

    private val viewModel by viewModels<AllInstalledAppsViewModel>()
    private var _binding: FragmentAllInstalledAppsBinding? = null
    private val binding get() = _binding
    private var allInstallAppsItemsAdapter: AllInstallAppsItemsAdapter? = null
    private var lastObservedSysApps: List<AllAppsEntity?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        allInstallAppsItemsAdapter = AllInstallAppsItemsAdapter(callback = {
            if (context?.isInternetAvailable()==true){
                if (findNavController().currentDestination?.id == R.id.navigation_all_installed_apps) {
                    val bundle = Bundle()
                    bundle.putParcelable(AppConstants.KEY_INSTALL_APP_DETAIL, it)
                    findNavController().navigate(
                        R.id.action_navigation_all_installed_apps_to_navigation_install_app_detail,
                        bundle
                    )
                }
            }else{
                activity?.toast(getString(R.string.no_internet))
            }

        }, dataList = arrayListOf())
        viewModel.invokeAllDeviceAppsUseCase()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAllInstalledAppsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            headerLayout.btnHeaderBackArrow.setOnClickListener {
                findNavController().navigateUp()
            }
            headerLayout.tvHeaderVersion.text = resources.getString(R.string.installed_apps)
            setupVersionsRecyclerView()
            observeVersionsData()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeVersionsData() {
        observeVersionsDataState()
        observeVersions()
    }

    private fun setupVersionsRecyclerView() {
        binding?.rvInstallApps?.apply {
            adapter = allInstallAppsItemsAdapter
            hasFixedSize()
        }
    }

    private fun observeVersionsDataState() {
        viewModel.mState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                handleState(state)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun observeVersions() {
        viewModel.sysAppsLists
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { versionEntity ->
                if (versionEntity != lastObservedSysApps) {
                    lastObservedSysApps = versionEntity
                    handleWallpapers(versionEntity)
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun handleWallpapers(versionsEntity: List<AllAppsEntity?>) {
        binding?.rvInstallApps?.adapter?.let {
            if (it is AllInstallAppsItemsAdapter) {
                versionsEntity.let { it1 -> it.updateVersionsList(it1) }
            }
        }
    }

    private fun handleState(state: AllInstallAppFragmentState) {
        when (state) {
            is AllInstallAppFragmentState.IsLoading -> handleLoading(state.isLoading)
            is AllInstallAppFragmentState.ShowToast -> context?.showToast(state.message)
            is AllInstallAppFragmentState.Init -> Unit
        }
    }

    private fun handleLoading(isLoading: Boolean) {
        if (isLoading) {
            binding?.loadingProgressBarVersions?.visible()
        } else {
            binding?.loadingProgressBarVersions?.gone()
        }
    }

}