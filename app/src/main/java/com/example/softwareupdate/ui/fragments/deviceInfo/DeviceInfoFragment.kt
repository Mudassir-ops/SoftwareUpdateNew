package com.example.softwareupdate.ui.fragments.deviceInfo

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.softwareupdate.R
import com.example.softwareupdate.databinding.FragmentDeviceInfoBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeviceInfoFragment : Fragment() {
    private var  _binding: FragmentDeviceInfoBinding?=null
    private val binding get() = _binding
    private val viewModel:DeviceInfoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       _binding = FragmentDeviceInfoBinding.inflate(inflater,container,false)
        return binding?.root
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            icBack.setOnClickListener { findNavController().navigateUp() }
        }
        viewModel.deviceInfo.let {
            binding?.txtDeviceName?.text = it.value?.deviceBrand
            binding?.txtDeviceModel?.text = it.value?.deviceModel
            binding?.txtDeviceRam?.text = it.value?.deviceRam
            binding?.txtDeviceVersion?.text = "Android ${it.value?.deviceVersion}"
            binding?.txtDeviceStorage?.text = it.value?.availableStorage
            binding?.txtDeviceDeviceId?.text = it.value?.softwareId
        }


    }

}