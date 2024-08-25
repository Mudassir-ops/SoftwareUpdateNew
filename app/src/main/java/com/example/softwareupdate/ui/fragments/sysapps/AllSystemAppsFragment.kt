package com.example.softwareupdate.ui.fragments.sysapps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.softwareupdate.R
import com.example.softwareupdate.adapters.sysapps.MySysAppsEntity
import com.example.softwareupdate.adapters.sysapps.SysAppsItemsAdapter
import com.example.softwareupdate.databinding.FragmentAllSystemAppsBinding
import com.example.softwareupdate.utils.AppConstants.SYSTEM_APPS_PACKAGE
import com.example.softwareupdate.utils.all_extension.gone
import com.example.softwareupdate.utils.all_extension.showToast
import com.example.softwareupdate.utils.all_extension.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class AllSystemAppsFragment : Fragment() {

    private val viewModel: AllSystemAppsViewModel by activityViewModels()
    private var _binding: FragmentAllSystemAppsBinding? = null
    private val binding get() = _binding
    private var sysAppsItemsAdapter: SysAppsItemsAdapter? = null
    private var lastObservedSysApps: List<MySysAppsEntity?>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sysAppsItemsAdapter = SysAppsItemsAdapter(callback = {
            if (findNavController().currentDestination?.id == R.id.navigation_system_applications) {
                val bundle = Bundle()
                bundle.putParcelable(SYSTEM_APPS_PACKAGE, it)
                findNavController().navigate(
                    R.id.action_navigation_system_applications_to_navigation_system_applications_details,
                    bundle
                )
            }
        }, dataList = emptyList())


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAllSystemAppsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupVersionsRecyclerView()
        observeVersionsData()
        binding?.apply {
            headerLayout.btnHeaderBackArrow.setOnClickListener {
                findNavController().navigateUp()
            }
            headerLayout.tvHeaderVersion.text = getString(R.string.system_applications)

        }
    }

    private fun observeVersionsData() {
        observeVersionsDataState()
        observeVersions()
    }

    private fun setupVersionsRecyclerView() {
        binding?.rvVersion?.apply {
            adapter = sysAppsItemsAdapter
            hasFixedSize()
        }
    }

    private fun observeVersionsDataState() {
        viewModel.mState.flowWithLifecycle(this.lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                handleState(state)
            }.launchIn(this.lifecycleScope)
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


    private fun handleWallpapers(versionsEntity: List<MySysAppsEntity?>) {

        binding?.rvVersion?.adapter?.let {
            if (it is SysAppsItemsAdapter) {
                versionsEntity.let { it1 -> it.updateVersionsList(it1) }
            }
        }
    }

    private fun handleState(state: AllSystemFragmentState) {
        when (state) {
            is AllSystemFragmentState.IsLoading -> handleLoading(state.isLoading)
            is AllSystemFragmentState.ShowToast -> context?.showToast(state.message)
            is AllSystemFragmentState.Init -> Unit
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