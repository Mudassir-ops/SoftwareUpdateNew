package com.example.softwareupdate.ui.fragments.uninstallapps

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.softwareupdate.R
import com.example.softwareupdate.adapters.allapps.AllAppsEntity
import com.example.softwareupdate.adapters.allapps.uninstallapps.UnInstallAppsItemsAdapter
import com.example.softwareupdate.databinding.FragmentAllUnInstalledAppsBinding
import com.example.softwareupdate.utils.all_extension.gone
import com.example.softwareupdate.utils.all_extension.showToast
import com.example.softwareupdate.utils.all_extension.visible
import com.example.softwareupdate.utils.isAppInstalled
import com.example.softwareupdate.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class AllUnInstalledAppsFragment : Fragment() {
    private var packageNameToDelete: String = ""
    private var deleteCallback: ((Boolean) -> Unit)? = null
    private val viewModel by viewModels<AllUnInstalledAppsViewModel>()
    private var _binding: FragmentAllUnInstalledAppsBinding? = null
    private val binding get() = _binding
    private var totalAppsCount = 0
    private var unInstallAppsItemsAdapter: UnInstallAppsItemsAdapter? = null
    private var lastObservedSysApps: List<AllAppsEntity?>? = null
    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val success = when {
                context?.isAppInstalled(packageNameToDelete) == true -> {
                    false
                }

                else -> {
                    true
                }
            }
            deleteCallback?.invoke(success)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        unInstallAppsItemsAdapter =
            UnInstallAppsItemsAdapter(callbackSelection = { listOfItemLambda ->
                val selectedCount = listOfItemLambda.count { it?.isSelected == true }
                if (selectedCount > 0) {
                    binding?.unInstallBtn?.show()
                    binding?.appUninstallCount?.apply {
                        show()
                        text = selectedCount.toString()
                    }
                } else {
                    binding?.unInstallBtn?.gone()
                    binding?.appUninstallCount?.gone()
                }
            }, dataList = arrayListOf())
        viewModel.invokeAllUnInstallAppsUseCase()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAllUnInstalledAppsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            headerLayout.btnHeaderBackArrow.setOnClickListener {
                findNavController().navigateUp()
            }
            headerLayout.tvHeaderVersion.text = getString(R.string.uninstall_apps)
            setupVersionsRecyclerView()
            observeVersionsData()
            binding?.customSelectAll?.setOnClickListener {
                val selectAll = binding?.customSelectAll?.isChecked == true
                if (selectAll) {
                    binding?.unInstallBtn?.show()
                    binding?.appUninstallCount?.apply {
                        show()
                        text = totalAppsCount.toString()
                    }
                } else {
                    binding?.unInstallBtn?.gone()
                    binding?.appUninstallCount?.gone()
                }
                unInstallAppsItemsAdapter?.selectAllItems(selectAll)
            }
            binding?.unInstallBtn?.setOnClickListener {
                val selectedApps =
                    unInstallAppsItemsAdapter?.dataList?.filter { it?.isSelected == true }
                if (selectedApps?.isNotEmpty() == true) {
                    deleteSelectedApps(selectedApps)
                } else {
                    activity?.showToast(getString(R.string.no_apps_selected_for_uninstallation))
                }
            }
        }
    }

    private fun observeVersionsData() {
        observeVersionsDataState()
        observeVersions()
    }

    private fun setupVersionsRecyclerView() {
        binding?.rvUnInstallAdapter?.apply {
            adapter = unInstallAppsItemsAdapter
            hasFixedSize()
        }
    }

    private fun observeVersionsDataState() {
        viewModel.mState.flowWithLifecycle(
            viewLifecycleOwner.lifecycle,
            Lifecycle.State.STARTED
        )
            .onEach { state ->
                handleState(state)
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun observeVersions() {
        viewModel.allUnInstallApps.flowWithLifecycle(
            viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED
        ).onEach { versionEntity ->
            if (versionEntity != lastObservedSysApps) {
                lastObservedSysApps = versionEntity
                handleWallpapers(versionEntity)
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun handleWallpapers(versionsEntity: List<AllAppsEntity?>) {
        binding?.rvUnInstallAdapter?.adapter?.let {
            if (it is UnInstallAppsItemsAdapter) {
                totalAppsCount = versionsEntity.size
                binding?.tvTotalApps?.text =
                    getString(R.string.total_apps_count, versionsEntity.size)
                versionsEntity.let { it1 -> it.updateVersionsList(it1 as ArrayList<AllAppsEntity?>) }
            }
        }
    }

    private fun handleState(state: AllUnInstallAppFragmentState) {
        when (state) {
            is AllUnInstallAppFragmentState.IsLoading -> handleLoading(state.isLoading)
            is AllUnInstallAppFragmentState.ShowToast -> context?.showToast(state.message)
            is AllUnInstallAppFragmentState.Init -> Unit
        }
    }

    private fun handleLoading(isLoading: Boolean) {
        if (isLoading) {
            binding?.loadingProgressBarUnInstall?.visible()
        } else {
            binding?.loadingProgressBarUnInstall?.gone()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun deleteSelectedApps(selectedApps: List<AllAppsEntity?>?) {
        if (selectedApps.isNullOrEmpty()) {
            activity?.showToast("All selected apps have been uninstalled")
            return
        }
        val index = 0
        deleteNextApp(selectedApps, index)
    }

    private fun deleteNextApp(selectedApps: List<AllAppsEntity?>, index: Int) {
        if (index >= selectedApps.size) {
           // activity?.showToast("All selected apps have been uninstalled")
            return
        }
        val app = selectedApps[index]
        app?.pName?.let { packageName ->
            deleteApp(packageName) { success ->
                if (success) {
                    val position =
                        unInstallAppsItemsAdapter?.dataList?.indexOfFirst { it?.pName == app.pName }
                    if (position != -1) {
                        position?.let { unInstallAppsItemsAdapter?.dataList?.removeAt(it) }
                        position?.let { unInstallAppsItemsAdapter?.notifyItemRemoved(it) }
                        unInstallAppsItemsAdapter?.dataList?.size?.let {
                            if (position != null) {
                                unInstallAppsItemsAdapter?.notifyItemRangeChanged(position, it)
                                val uninstallBtnCount =
                                    binding?.appUninstallCount?.text.toString().toInt().minus(1)
                                        .toString()

                                val inputString = binding?.tvTotalApps?.text.toString()
                                val numberString = inputString.filter { it.isDigit() }
                                val number = numberString.toIntOrNull()
                                binding?.tvTotalApps?.text = number?.minus(1).toString()
                                if (uninstallBtnCount.toInt() > 0) {
                                    binding?.appUninstallCount?.text = uninstallBtnCount
                                } else {
                                    binding?.appUninstallCount?.gone()
                                    binding?.unInstallBtn?.gone()
                                }
                            }
                        }
                    }
                }
                deleteNextApp(selectedApps, index + 1)
            }
        }
    }

    private fun deleteApp(packageName: String, callback: (Boolean) -> Unit) {
        deleteCallback = callback
        packageNameToDelete = packageName
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = Uri.parse("package:$packageName")
        launcher.launch(intent)
    }

}