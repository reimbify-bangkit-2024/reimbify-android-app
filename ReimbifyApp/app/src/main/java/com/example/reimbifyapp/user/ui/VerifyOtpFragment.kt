package com.example.reimbifyapp.user.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.reimbifyapp.MainActivityUser
import com.example.reimbifyapp.R
import com.example.reimbifyapp.databinding.FragmentVerifyOtpBinding
import com.example.reimbifyapp.user.data.entities.User
import com.example.reimbifyapp.user.utils.children
import com.example.reimbifyapp.user.viewmodel.LoginViewModel
import com.example.reimbifyapp.user.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class VerifyOtpFragment : Fragment(R.layout.fragment_verify_otp) {
    private var _binding: FragmentVerifyOtpBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(requireActivity())
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
                    User(
                        userId = response.userId,
                        token = response.accessToken,
                        role = response.role,
                        isLogin = true
                    )
                )
                showToast("Login successful!")
                moveToDashboard()
            }

            result.onFailure { throwable ->
                handleLoginError(throwable)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupActions(userId: String) {
        binding.verifyButton.setOnClickListener {
            val otp = binding.otpEditText.getOtp()

            if (validateInput(otp)) {
                showLoading(true)
                viewModel.verifyOtp(userId, otp)
            }
        }
    }

    private fun validateInput(otp: String): Boolean {
        if (otp.length != 6) {
            showToast("Invalid OTP")
            return false
        }

        return true
    }

    private fun moveToDashboard() {
        // TODO: Navigate either to User Dashboard or Admin Dashboard
        lifecycleScope.launch {
            val user = viewModel.getSession().first()
            when (user.role) {
                "user" -> {
                    showToast("Navigating to User Dashboard")
                    val intent = Intent(requireContext(), MainActivityUser::class.java).apply {
                        putExtra("open_fragment", "dashboard")
                    }
                    startActivity(intent)
                    requireActivity().finish()
                }
                "admin" -> {
                    showToast("Navigating to Admin Dashboard")
                }
            }
        }
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