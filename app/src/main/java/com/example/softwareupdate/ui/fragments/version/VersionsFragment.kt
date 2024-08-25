package com.example.softwareupdate.ui.fragments.version

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.softwareupdate.R
import com.example.softwareupdate.adapters.version.VersionsEntity
import com.example.softwareupdate.adapters.version.VersionsItemsAdapter
import com.example.softwareupdate.databinding.FragmentVersionsBinding
import com.example.softwareupdate.utils.AppConstants.KEY_VERSION_DETAIL
import com.example.softwareupdate.utils.all_extension.gone
import com.example.softwareupdate.utils.all_extension.showToast
import com.example.softwareupdate.utils.all_extension.toast
import com.example.softwareupdate.utils.all_extension.visible
import com.example.softwareupdate.utils.isInternetAvailable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class VersionsFragment : Fragment() ,VersionsItemsAdapter.VersionsItemsAdapterCallback{


    private val viewModel by viewModels<VersionViewModel>()
    private var _binding: FragmentVersionsBinding? = null
    private val binding get() = _binding
    private var versionsItemsAdapter: VersionsItemsAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        versionsItemsAdapter = VersionsItemsAdapter(callback = {
            if (context?.isInternetAvailable() == true){
                val bundle = Bundle()
                bundle.putParcelable(KEY_VERSION_DETAIL, it)
                if (findNavController().currentDestination?.id == R.id.navigation_api_versions) {
                    findNavController().navigate(
                        R.id.action_navigation_api_versions_to_versionDetailsFragment, bundle
                    )
                }
            }else{
                activity?.toast(getString(R.string.no_internet))
            }

        }, dataList = emptyList(), adapterCallback = this)
        viewModel.getAllVersions(context ?: return)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVersionsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            headerLayout.tvHeaderVersion.text = getString(R.string.android_versions)
            headerLayout.btnHeaderBackArrow.setOnClickListener {
                findNavController().navigateUp()
            }
            setupVersionsRecyclerView()
            observeVersionsData()
            etSearch.doAfterTextChanged {
                versionsItemsAdapter?.filter?.filter(it.toString())
            }
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
        binding?.rvVersion?.apply {
            adapter = versionsItemsAdapter
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
        viewModel.versionList.flowWithLifecycle(this.lifecycle, Lifecycle.State.STARTED)
            .onEach { versionEntity ->
                handleWallpapers(versionEntity)
            }.launchIn(this.lifecycleScope)
    }

    private fun handleWallpapers(versionsEntity: List<VersionsEntity?>) {
        binding?.rvVersion?.adapter?.let {
            if (it is VersionsItemsAdapter) {
                versionsEntity.let { it1 -> it.updateVersionsList(it1) }
            }
        }
    }

    private fun handleState(state: VersionFragmentState) {
        when (state) {
            is VersionFragmentState.IsLoading -> handleLoading(state.isLoading)
            is VersionFragmentState.ShowToast -> context?.showToast(state.message)
            is VersionFragmentState.Init -> Unit
        }
    }

    private fun handleLoading(isLoading: Boolean) {
        if (isLoading) {
            binding?.loadingProgressBarVersions?.visible()
        } else {
            binding?.loadingProgressBarVersions?.gone()
        }
    }

    override fun onEmptySearchResults(isEmpty: Boolean) {
        binding?.txtNoMatchFound?.visibility = if (isEmpty) View.VISIBLE else View.GONE

    }
}