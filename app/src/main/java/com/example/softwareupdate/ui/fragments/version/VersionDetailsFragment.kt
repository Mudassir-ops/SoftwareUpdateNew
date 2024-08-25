package com.example.softwareupdate.ui.fragments.version

import android.R
import android.os.Bundle
import android.text.SpannableString
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.softwareupdate.adapters.version.VersionsEntity
import com.example.softwareupdate.databinding.FragmentVersionDetailsBinding
import com.example.softwareupdate.utils.AppConstants
import com.example.softwareupdate.utils.TextJustification.justify
import com.example.softwareupdate.utils.parcelable


class VersionDetailsFragment : Fragment() {

    private val viewModel by viewModels<VersionDetailsViewModel>()
    private var _binding: FragmentVersionDetailsBinding? = null
    private val binding get() = _binding
    private var versionsEntity: VersionsEntity? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        versionsEntity = arguments?.parcelable(AppConstants.KEY_VERSION_DETAIL)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVersionDetailsBinding.inflate(inflater, container, false)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            headerLayout.tvHeaderVersion.text = versionsEntity?.versionTitle
            headerLayout.btnHeaderBackArrow.setOnClickListener {
                findNavController().navigateUp()
            }
            tvScrollableVersionDetail.text = versionsEntity?.versionOverview
            tvScrollableVersionDetail.movementMethod = ScrollingMovementMethod()


        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}