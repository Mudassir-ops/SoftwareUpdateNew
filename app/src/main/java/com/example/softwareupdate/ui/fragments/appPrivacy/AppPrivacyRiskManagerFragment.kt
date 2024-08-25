package com.example.softwareupdate.ui.fragments.appPrivacy

import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import com.example.softwareupdate.adapters.privacymanager.PrivacyManagerEntity
import com.example.softwareupdate.adapters.privacymanager.PrivacyManagerItemsAdapter
import com.example.softwareupdate.databinding.FragmentAppPrivacyRiskManagerBinding
import com.example.softwareupdate.utils.AppConstants.PRIVACY_MANGER_OBJECT
import com.example.softwareupdate.utils.PopupOptionSelector
import com.example.softwareupdate.utils.all_extension.gone
import com.example.softwareupdate.utils.all_extension.showToast
import com.example.softwareupdate.utils.all_extension.visible
import com.example.softwareupdate.utils.calculateRiskValues
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


@AndroidEntryPoint
class AppPrivacyRiskManagerFragment : Fragment() {

    private val viewModel: AppPrivacyRiskManagerViewModel by activityViewModels()
    private var _binding: FragmentAppPrivacyRiskManagerBinding? = null
    private var privacyManagerItemsAdapter: PrivacyManagerItemsAdapter? = null
    private val binding get() = _binding
    private var lastObservedSysApps: List<PrivacyManagerEntity?>? = null
    private var popupOptionSelector: PopupOptionSelector? = null
    private var selectedSpinnerPosition = 1
    private var TAG = "AppPrivacyRiskManagerFragment"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG, "onCreate: onCreate", )
        popupOptionSelector = PopupOptionSelector(context ?: return)
        privacyManagerItemsAdapter = PrivacyManagerItemsAdapter(callback = {
            if (findNavController().currentDestination?.id == R.id.app_privacy_manager) {
                val bundle = Bundle()
                bundle.putParcelable(PRIVACY_MANGER_OBJECT, it)
                findNavController().navigate(
                    R.id.action_app_privacy_manager_to_navigation_privacy_manager_detail, bundle
                )
            }
        }, dataList = arrayListOf(), callbackPercentage = { totalSize ->
            binding?.tvPercentage?.text =
                String.format(getString(R.string.percentage_placeholder), totalSize)
        }, context = context ?: return
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAppPrivacyRiskManagerBinding.inflate(inflater, container, false)

        Log.e(TAG, "onCreateView: onCreateView", )

        return binding?.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Save the current risk values and spinner position
        val resultValues = lastObservedSysApps?.calculateRiskValues() ?: Triple(0, 0, 0)
        outState.putInt("highRiskValue", resultValues.first)
        outState.putInt("averageRiskValue", resultValues.second)
        outState.putInt("lowRiskValue", resultValues.third)
        outState.putInt("selectedSpinnerPosition", selectedSpinnerPosition)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupVersionsRecyclerView()

        binding?.apply {
            headerLayout.tvHeaderVersion.text = getString(R.string.privacy_manager)
            tvPercentageValue.isSelected = true
            tvHighRiskIndicator.isSelected = true
            tvAvgRiskIndicator.isSelected = true
            tvLowRiskIndicator.isSelected = true
            headerLayout.btnHeaderBackArrow.setOnClickListener {
                findNavController().navigateUp()
            }

            layoutSelector.setOnClickListener {
                popupOptionSelector?.showPopup(layoutSelector, R.menu.popup_menu) { position ->
                    if (selectedSpinnerPosition != position) {
                        selectedSpinnerPosition = position
                        lastObservedSysApps?.let { it1 -> handleData(it1) }
                    }
                    val menuItem = context?.resources?.getStringArray(R.array.privacy_manager_array)
                        ?.get(position)
                    val menuItemProgressText =
                        context?.resources?.getStringArray(R.array.privacy_manager_array_progress_text)
                            ?.get(position)
                    tvSelector.text = menuItem
                    tvPercentageValue.text = menuItemProgressText
                }
            }

            // Restore saved state if available
            if (savedInstanceState != null) {
                val highRiskValue = savedInstanceState.getInt("highRiskValue")
                val averageRiskValue = savedInstanceState.getInt("averageRiskValue")
                val lowRiskValue = savedInstanceState.getInt("lowRiskValue")
                selectedSpinnerPosition = savedInstanceState.getInt("selectedSpinnerPosition")

                // Set the chart with the restored data
                setupPieChart(pieChart, highRiskValue, averageRiskValue, lowRiskValue)
            } else {
                observeVersionsData()
            }

        }
        binding?.pieChart?.let { pieChart ->
            pieChart.post {
                val resultValues = lastObservedSysApps?.calculateRiskValues() ?: Triple(0, 0, 0)
                setupPieChart(
                    pieChart, resultValues.first, resultValues.second, resultValues.third
                )
            }
        }
    }

    private fun observeVersionsData() {
        observeVersionsDataState()
        observeVersions()
    }

    private fun setupVersionsRecyclerView() {
        binding?.rvInstallApps?.apply {
            adapter = privacyManagerItemsAdapter
            hasFixedSize()
        }
    }

    private fun observeVersionsDataState() {
        viewModel.mState.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                handleState(state)
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun observeVersions() {
        viewModel.appsLists.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { versionEntity ->
                if (versionEntity != lastObservedSysApps) {
                    lastObservedSysApps = versionEntity
                    handleData(versionEntity)

                    // Ensure the chart is updated with the correct values when data changes
                    val resultValues = versionEntity.calculateRiskValues()
                    binding?.pieChart?.let { pieChart ->
                        setupPieChart(
                            pieChart, resultValues.first, resultValues.second, resultValues.third
                        )
                    }
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }
//    private fun observeVersions() {
//        viewModel.appsLists.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
//            .onEach { versionEntity ->
//                if (versionEntity != lastObservedSysApps) {
//                    lastObservedSysApps = versionEntity
//                    handleData(versionEntity)
//                    val resultValues = versionEntity.calculateRiskValues()
//                    binding?.pieChart?.let { it1 ->
//                        setupPieChart(
//                            it1, resultValues.first, resultValues.second, resultValues.third
//                        )
//                    }
//                }
//            }.launchIn(viewLifecycleOwner.lifecycleScope)
//    }

    private fun handleData(dataList: List<PrivacyManagerEntity?>?) {
        binding?.rvInstallApps?.adapter?.let { adapter ->
            if (adapter is PrivacyManagerItemsAdapter) {
                dataList?.let { dataList ->
                    adapter.updateVersionsList(dataList, selectedSpinnerPosition)
                }
            }
        }
    }

    private fun handleState(state: PrivacyManagerFragmentState) {
        when (state) {
            is PrivacyManagerFragmentState.IsLoading -> handleLoading(state.isLoading)
            is PrivacyManagerFragmentState.ShowToast -> context?.showToast(state.message)
            is PrivacyManagerFragmentState.Init -> Unit
        }
    }

    private fun handleLoading(isLoading: Boolean) {
        if (isLoading) {
            binding?.loadingProgressBarVersions?.visible()
            binding?.chartLayout?.gone()
            binding?.tvPrivacy?.gone()
        } else {
            binding?.loadingProgressBarVersions?.gone()
            binding?.chartLayout?.visible()
            binding?.tvPrivacy?.visible()
        }
    }

   /* private fun setupPieChart(
        pieChart: PieChart, highRiskValue: Int, averageRiskValue: Int, lowRiskValue: Int
    ) {
        val total = highRiskValue + averageRiskValue + lowRiskValue
        val highRiskPercentage = if (total != 0) (highRiskValue.toDouble() / total) * 100 else 0.0
        val averageRiskPercentage =
            if (total != 0) (averageRiskValue.toDouble() / total) * 100 else 0.0
        val lowRiskPercentage = if (total != 0) (lowRiskValue.toDouble() / total) * 100 else 0.0
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(highRiskPercentage.toFloat(), "High Risk"))
        entries.add(PieEntry(averageRiskPercentage.toFloat(), "Average Risk"))
        entries.add(PieEntry(lowRiskPercentage.toFloat(), "Low Risk"))


        Log.d(TAG, "High Risk: $highRiskPercentage%")
        Log.d(TAG, "Average Risk: $averageRiskPercentage%")
        Log.d(TAG, "Low Risk: $lowRiskPercentage%")

        val colors = listOf(Color.RED, Color.BLUE, Color.GREEN)
        val dataSet = PieDataSet(entries, null).apply {
            this.colors = colors
        }
            //  val dataSet = PieDataSet(entries, null)
        dataSet.colors = colors
        val data = PieData(dataSet)
        data.setValueTextColor(Color.WHITE)
        data.setValueTextSize(0f)
        pieChart.clear()
        pieChart.data = data
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = false
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.setDrawEntryLabels(false)
        pieChart.setExtraOffsets(5f, 0f, 5f, 0f)
        pieChart.holeRadius = 74f
        pieChart.transparentCircleRadius = 0f
        pieChart.isRotationEnabled = false
        pieChart.invalidate()
    }*/

    private fun setupPieChart(
        pieChart: PieChart, highRiskValue: Int, averageRiskValue: Int, lowRiskValue: Int
    ) {
        val total = highRiskValue + averageRiskValue + lowRiskValue
        val highRiskPercentage = if (total != 0) (highRiskValue.toDouble() / total) * 100 else 0.0
        val averageRiskPercentage =
            if (total != 0) (averageRiskValue.toDouble() / total) * 100 else 0.0
        val lowRiskPercentage = if (total != 0) (lowRiskValue.toDouble() / total) * 100 else 0.0
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(highRiskPercentage.toFloat(), "High Risk"))
        entries.add(PieEntry(averageRiskPercentage.toFloat(), "Average Risk"))
        entries.add(PieEntry(lowRiskPercentage.toFloat(), "Low Risk"))

        val colors = listOf(Color.RED, Color.BLUE, Color.GREEN)
        val dataSet = PieDataSet(entries, null).apply {
            this.colors = colors
        }
        val data = PieData(dataSet).apply {
            setValueTextColor(Color.WHITE)
            setValueTextSize(0f)
        }

        pieChart.clear()
        pieChart.data = data
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = false
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.setDrawEntryLabels(false)
        pieChart.setExtraOffsets(5f, 0f, 5f, 0f)
        pieChart.holeRadius = 74f
        pieChart.transparentCircleRadius = 0f
        pieChart.isRotationEnabled = false
        pieChart.invalidate()
    }



}