package com.example.softwareupdate.ui.fragments.availableupdates

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
import com.example.softwareupdate.adapters.appupdate.UpdatedAppsItemsAdapter
import com.example.softwareupdate.data.PackageInfoEntity
import com.example.softwareupdate.databinding.FragmentAvailableUpdatedAppBinding
import com.example.softwareupdate.utils.all_extension.gone
import com.example.softwareupdate.utils.all_extension.showToast
import com.example.softwareupdate.utils.all_extension.visible
import com.example.softwareupdate.utils.openAppInPlayStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class AvailableUpdatedAppFragment : Fragment() {

    private val viewModel by viewModels<AvailableUpdatedAppViewModel>()
    private var _binding: FragmentAvailableUpdatedAppBinding? = null
    private val binding get() = _binding
    private var updatedAppsItemsAdapter: UpdatedAppsItemsAdapter? = null
    private var lastObservedSysApps: List<PackageInfoEntity?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updatedAppsItemsAdapter = UpdatedAppsItemsAdapter(callback = {
            context?.openAppInPlayStore(it.pName)
        }, dataList = arrayListOf())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAvailableUpdatedAppBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            headerLayout.btnHeaderBackArrow.setOnClickListener {
                findNavController().navigateUp()
            }
            headerLayout.tvHeaderVersion.text = resources.getString(R.string.available_updates)
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
        observeUpdatedApps()
    }

    private fun setupVersionsRecyclerView() {
        binding?.rvInstallApps?.apply {
            adapter = updatedAppsItemsAdapter
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

    private fun observeUpdatedApps() {
        viewModel.appsLists
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { versionEntity ->
                if (versionEntity != lastObservedSysApps) {
                    lastObservedSysApps = versionEntity
                    handleUpdatedApps(versionEntity)
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun handleUpdatedApps(versionsEntity: List<PackageInfoEntity?>) {
        binding?.rvInstallApps?.adapter?.let {
            if (it is UpdatedAppsItemsAdapter) {
                versionsEntity.let { it1 -> it.updateVersionsList(it1) }
            }
        }
    }

    private fun handleState(state: UpdatedAppFragmentState) {
        when (state) {
            is UpdatedAppFragmentState.IsLoading -> handleLoading(state.isLoading)
            is UpdatedAppFragmentState.ShowToast -> context?.showToast(state.message)
            is UpdatedAppFragmentState.Init -> Unit
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