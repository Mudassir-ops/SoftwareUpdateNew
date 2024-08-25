package com.example.softwareupdate.ui.fragments.appPrivacy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.softwareupdate.adapters.privacymanager.PrivacyManagerDetailItemsAdapter
import com.example.softwareupdate.adapters.privacymanager.PrivacyManagerEntity
import com.example.softwareupdate.databinding.FragmentPrivacyManagerDetailBinding
import com.example.softwareupdate.utils.AppConstants.PRIVACY_MANGER_OBJECT
import com.example.softwareupdate.utils.all_extension.gone
import com.example.softwareupdate.utils.parcelable

class PrivacyManagerDetailFragment : Fragment() {

    private val viewModel by viewModels<PrivacyManagerDetailViewModel>()
    private var _binding: FragmentPrivacyManagerDetailBinding? = null
    private val binding get() = _binding
    private var privacyManagerEntity: PrivacyManagerEntity? = null
    private var privacyManagerDetailItemsAdapter: PrivacyManagerDetailItemsAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        privacyManagerEntity = arguments?.parcelable(PRIVACY_MANGER_OBJECT)
        privacyManagerDetailItemsAdapter =
            PrivacyManagerDetailItemsAdapter(callback = {}, dataList = arrayListOf())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPrivacyManagerDetailBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            headerLayout.btnHeaderBackArrow.setOnClickListener {
                findNavController().navigateUp()
            }
            ivAppIcon.setImageDrawable(privacyManagerEntity?.icon)
            tvAppName.text = privacyManagerEntity?.appName
            tvAppSize.text = "20Mb"
            headerLayout.tvHeaderVersion.gone()
        }
        setupVersionsRecyclerView()
        handleData()
    }

    private fun setupVersionsRecyclerView() {
        binding?.rvPrivacyManagerDetail?.apply {
            adapter = privacyManagerDetailItemsAdapter
            hasFixedSize()
        }
    }

    private fun handleData() {
        binding?.rvPrivacyManagerDetail?.adapter?.let {
            if (it is PrivacyManagerDetailItemsAdapter) {
                privacyManagerEntity?.let { it1 ->
                    it1.permissionList?.let { it2 ->
                        it.updateVersionsList(
                            it2
                        )
                    }
                }
            }
        }
    }
}