package com.example.reimbifyapp.auth.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.reimbifyapp.R
import com.example.reimbifyapp.databinding.FragmentVerifyOtpBinding
import com.example.reimbifyapp.data.entities.UserSession
import com.example.reimbifyapp.utils.children
import com.example.reimbifyapp.auth.viewmodel.LoginViewModel
import com.example.reimbifyapp.auth.factory.UserViewModelFactory

class VerifyOtpFragment : Fragment(R.layout.fragment_verify_otp) {
    private var _binding: FragmentVerifyOtpBinding? = null
    private val binding get() = _binding!!

    private var otp: String? = null

    private val viewModel by viewModels<LoginViewModel> {
        UserViewModelFactory.getInstance(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerifyOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = arguments?.getString("userId") ?: ""
        if (userId.isEmpty()) {
            showToast("Failed to retrieve User ID")
            return
        }

        setupActions(userId)
        playAnimation()

        viewModel.verifyOtpResult.observe(viewLifecycleOwner) { result ->
            showLoading(false)

            result.onSuccess { response ->
                viewModel.saveSession(
                    UserSession(
                        userId = response.userId,
                        token = response.accessToken,
                        role = response.role,
                        isLogin = true
                    )
                )

                showToast("User Verified!")
                val bundle = Bundle().apply {
                    putString("userId", response.userId)
                    putString("otp", otp)
                }

                findNavController().navigate(
                    R.id.action_otpVerificationFragment_to_resetPasswordFragment,
                    bundle
                )
            }

            result.onFailure { throwable ->
                handleLoginError(throwable)
            }
        }

        viewModel.sendOtpResult.observe(viewLifecycleOwner) { result ->
            showLoading(false)

            result.onSuccess {
                showToast("A new OTP has been successfully sent to your email")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupActions(userId: String) {
        binding.verifyButton.setOnClickListener {
            otp = binding.otpEditText.getOtp()

            if (validateInput(otp)) {
                showLoading(true)
                viewModel.verifyOtp(userId, otp!!)
            }
        }

        binding.resendOTPTextView.setOnClickListener {
            showLoading(true)
            viewModel.sendOtp(userId)
        }
    }

    private fun validateInput(otp: String?): Boolean {
        if (otp.isNullOrEmpty()) {
            showToast("OTP cannot be empty")
            return false
        }

        if (otp.length != 6) {
            showToast("Invalid OTP")
            return false
        }

        return true
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingOverlay.visibility = View.VISIBLE
            binding.progressBar.isIndeterminate = true
        } else {
            binding.loadingOverlay.visibility = View.GONE
        }
    }

    private fun handleLoginError(throwable: Throwable) {
        Toast.makeText(
            requireContext(),
            throwable.localizedMessage ?: "An unknown error occurred",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun playAnimation() {
        val imageAnimation = ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }

        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).apply { duration = 200 }
        val subtitle = ObjectAnimator.ofFloat(binding.messageTextView, View.ALPHA, 1f).apply { duration = 200 }
        val otpAnimations = (binding.otpEditText as ViewGroup).children.map { child ->
            ObjectAnimator.ofFloat(child, View.ALPHA, 1f).apply { duration = 200 }
        }
        val verify = ObjectAnimator.ofFloat(binding.verifyButton, View.ALPHA, 1f).apply { duration = 200 }

        AnimatorSet().apply {
            playSequentially(
                ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, 0f, 0f).apply { duration = 300 },
                title,
                subtitle,
                *otpAnimations.toTypedArray(),
                verify
            )
            start()
        }

        imageAnimation.start()
    }
}