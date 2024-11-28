package com.example.reimbifyapp.user.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.reimbifyapp.MainActivityUser
import com.example.reimbifyapp.R
import com.example.reimbifyapp.databinding.FragmentForgotPasswordBinding
import com.example.reimbifyapp.databinding.FragmentLoginBinding
import com.example.reimbifyapp.user.data.entities.User
import com.example.reimbifyapp.user.utils.ErrorUtils.parseErrorMessage
import com.example.reimbifyapp.user.viewmodel.LoginViewModel
import com.example.reimbifyapp.user.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ForgotPasswordFragment : Fragment(R.layout.fragment_forgot_password) {
    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActions()
        playAnimation()

        viewModel.forgotPasswordResult.observe(viewLifecycleOwner) { result ->
            showLoading(false)

            result.onSuccess { response ->
                showToast("OTP sent successfully!")
                val bundle = Bundle().apply {
                    putString("userId", response.userId)
                }

                findNavController().navigate(
                    R.id.action_forgotPasswordFragment_to_otpVerificationFragment,
                    bundle
                )
            }

            result.onFailure { throwable ->
                val errorMessage = parseErrorMessage(throwable)
                showToast(errorMessage)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupActions() {
        binding.sendOtpButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val username = binding.usernameEditText.text.toString().trim()

            if (validateInput(email, username)) {
                showLoading(true)
                viewModel.forgotPassword(email, username)
            }
        }
    }


    private fun validateInput(email: String, username: String): Boolean {
        if (email.isEmpty()) {
            showToast("Please enter your email")
            return false
        }

        if (binding.emailEditTextLayout.error != null) {
            showToast("Please enter a valid email address")
            return false
        }

        if (username.isEmpty()) {
            showToast("Username cannot be empty")
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

    private fun playAnimation() {
        val imageAnimation = ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }

        val emailTv = ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).apply { duration = 200 }
        val emailLayout = ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).apply { duration = 200 }
        val usernameTv = ObjectAnimator.ofFloat(binding.usernameTextView, View.ALPHA, 1f).apply { duration = 200 }
        val usernameLayout = ObjectAnimator.ofFloat(binding.usernameEditTextLayout, View.ALPHA, 1f).apply { duration = 200 }
        val sendOtp = ObjectAnimator.ofFloat(binding.sendOtpButton, View.ALPHA, 1f).apply { duration = 200 }

        AnimatorSet().apply {
            playSequentially(
                ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, 0f, 0f).apply { duration = 300 },
                emailTv,
                emailLayout,
                usernameTv,
                usernameLayout,
                sendOtp
            )
            start()
        }

        imageAnimation.start()
    }
}