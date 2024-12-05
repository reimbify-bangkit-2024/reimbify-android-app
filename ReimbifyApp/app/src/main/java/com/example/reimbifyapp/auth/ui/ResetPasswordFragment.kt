package com.example.reimbifyapp.auth.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.reimbifyapp.R
import com.example.reimbifyapp.databinding.FragmentResetPasswordBinding
import com.example.reimbifyapp.auth.ui.component.CustomConfirmPasswordEditText
import com.example.reimbifyapp.utils.ErrorUtils.parseErrorMessage
import com.example.reimbifyapp.auth.viewmodel.LoginViewModel
import com.example.reimbifyapp.auth.factory.UserViewModelFactory

class ResetPasswordFragment : Fragment(R.layout.fragment_reset_password) {
    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: CustomConfirmPasswordEditText

    private val viewModel by viewModels<LoginViewModel> {
        UserViewModelFactory.getInstance(requireActivity())
    }

    private var lastToastTime = 0L
    private val toastDelay = 5000L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = arguments?.getString("userId")
        val otp = arguments?.getString("otp")

        if (userId.isNullOrEmpty() || otp.isNullOrEmpty()) {
            showToast("Missing data Bundle! Please try again")
            findNavController().navigate(
                R.id.resetPasswordFragment_to_forgotPasswordFragment
            )
        }

        playAnimation()
        if (userId != null && otp != null ) {
            setupActions(userId, otp)
        }

        newPasswordEditText = binding.newPasswordEditText
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText)
        newPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                confirmPasswordEditText.setOriginalPassword(s?.toString() ?: "")
            }
        })

        viewModel.resetPasswordResult.observe(viewLifecycleOwner) { result ->
            showLoading(false)

            result.onSuccess {
                showToast("Reset password successfully!")
                findNavController().navigate(
                    R.id.resetPasswordFragment_to_loginFragment
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

    private fun setupActions(userId: String, otp: String) {
        binding.resetPasswordButton.setOnClickListener {
            val newPassword = binding.newPasswordEditText.text.toString().trim()
            val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()

            if (validateInput(newPassword, confirmPassword)) {
                showLoading(true)
                viewModel.resetPassword(userId, otp, newPassword)
            }
        }
    }


    private fun validateInput(newPassword: String, confirmPassword: String): Boolean {
        if (newPassword.isEmpty()) {
            showToast("Please enter your new password")
            return false
        }

        if (confirmPassword.isEmpty()) {
            showToast("Please enter your new password confirmation")
            return false
        }

        if (newPassword != confirmPassword) {
            showToast("Password do not match!")
            return false
        }

        return true
    }

    private fun showToast(message: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastToastTime > toastDelay) {
            lastToastTime = currentTime
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
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

        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).apply { duration = 200 }
        val newPasswordTv = ObjectAnimator.ofFloat(binding.newPasswordEditText, View.ALPHA, 1f).apply { duration = 200 }
        val newPasswordLayout = ObjectAnimator.ofFloat(binding.newPasswordEditTextLayout, View.ALPHA, 1f).apply { duration = 200 }
        val confirmPasswordTv = ObjectAnimator.ofFloat(binding.confirmPasswordEditText, View.ALPHA, 1f).apply { duration = 200 }
        val confirmPasswordLayout = ObjectAnimator.ofFloat(binding.confirmPasswordEditTextLayout, View.ALPHA, 1f).apply { duration = 200 }
        val resetPassword = ObjectAnimator.ofFloat(binding.resetPasswordButton, View.ALPHA, 1f).apply { duration = 200 }

        AnimatorSet().apply {
            playSequentially(
                ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, 0f, 0f).apply { duration = 300 },
                title,
                newPasswordTv,
                newPasswordLayout,
                confirmPasswordTv,
                confirmPasswordLayout,
                resetPassword
            )
            start()
        }

        imageAnimation.start()
    }
}