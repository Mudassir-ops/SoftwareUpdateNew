package com.example.softwareupdate.ui.fragments.language

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
import com.example.softwareupdate.adapters.langauge.LanguageItemsModel
import com.example.softwareupdate.adapters.langauge.LanguageScreensItemsAdapter
import com.example.softwareupdate.databinding.FragmentLanguageBinding
import com.example.softwareupdate.utils.all_extension.gone
import com.example.softwareupdate.utils.all_extension.showToast
import com.example.softwareupdate.utils.all_extension.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class LanguageFragment : Fragment() {

    private val viewModel by viewModels<LanguageViewModel>()
    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding
    private var languageScreensItemsAdapter: LanguageScreensItemsAdapter? = null
    private var lastObservedSysApps: List<LanguageItemsModel?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        languageScreensItemsAdapter = LanguageScreensItemsAdapter(callback = {
            languageScreensItemsAdapter?.updateSelectedIndex(it)
        }, dataList = arrayListOf())
        viewModel.getAllUpdatedApp(context ?: return)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLanguageBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            headerLayout.btnHeaderBackArrow.setOnClickListener {
                findNavController().navigateUp()
            }
            btnNext.setOnClickListener {
                findNavController().navigateUp()
            }
            headerLayout.tvHeaderVersion.text = getString(R.string.choose_languages)
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
            adapter = languageScreensItemsAdapter
            hasFixedSize()
        }
    }

    private fun observeVersionsDataState() {
        viewModel.mState.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                handleState(state)
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun observeUpdatedApps() {
        viewModel.languageLists.flowWithLifecycle(
            viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED
        ).onEach { versionEntity ->
            if (versionEntity != lastObservedSysApps) {
                lastObservedSysApps = versionEntity
                handleUpdatedApps(versionEntity)
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun handleUpdatedApps(versionsEntity: List<LanguageItemsModel?>) {
        binding?.rvInstallApps?.adapter?.let {
            if (it is LanguageScreensItemsAdapter) {
                versionsEntity.let { it1 -> it.updateLanguageLists(it1) }
            }
        }
    }

    private fun handleState(state: LanguageFragmentState) {
        when (state) {
            is LanguageFragmentState.IsLoading -> handleLoading(
                state.isLoading
            )

            is LanguageFragmentState.ShowToast -> context?.showToast(
                state.message
            )

            is LanguageFragmentState.Init -> Unit
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