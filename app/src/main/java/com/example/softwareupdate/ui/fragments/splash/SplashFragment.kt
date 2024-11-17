package com.example.softwareupdate.ui.fragments.splash

import android.animation.Animator
import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.softwareupdate.R
import com.example.softwareupdate.databinding.FragmentSplashBinding
import com.example.softwareupdate.ui.fragments.home.HomeViewModel
import com.example.softwareupdate.utils.AppConstants.IF_FIRST_TIME_OPEN_APP
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO


@AndroidEntryPoint
class SplashFragment : Fragment() {
    private var animator: ValueAnimator? = null
    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        IF_FIRST_TIME_OPEN_APP = true
        binding?.apply {
            animator = ValueAnimator.ofInt(0, progressHorizontal.max)
            animator?.duration = 3000
            animator?.addUpdateListener { animation ->
                progressHorizontal.progress = animation.animatedValue as Int
            }

            animator?.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator) {}
                override fun onAnimationEnd(p0: Animator) {
                    Log.e(
                        "onAnimationEnd",
                        "onAnimationEnd: ${progressHorizontal.progress}---${progressHorizontal.max}--$p0"
                    )
                    if (progressHorizontal.progress == progressHorizontal.max) {
                        if (findNavController().currentDestination?.id == R.id.navigation_splash) {
                            findNavController().navigate(R.id.action_navigation_splash_to_navigation_home)
                        }
                    }
                }

                override fun onAnimationCancel(p0: Animator) {}

                override fun onAnimationRepeat(p0: Animator) {}

            })
            animator?.start()
            settingIcon.setAnimation(R.raw.splash_lottie)
            settingIcon.playAnimation()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        animator = null
    }

    override fun onPause() {
        super.onPause()
        animator?.pause()
    }

    override fun onResume() {
        super.onResume()
        animator?.resume()
    }


}

