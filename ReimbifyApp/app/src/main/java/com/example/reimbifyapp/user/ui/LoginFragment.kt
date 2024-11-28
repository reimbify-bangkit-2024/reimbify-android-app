package com.example.reimbifyapp.user.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.reimbifyapp.MainActivityUser
import com.example.reimbifyapp.R
import com.example.reimbifyapp.databinding.FragmentLoginBinding
import com.example.reimbifyapp.user.data.entities.User
import com.example.reimbifyapp.user.utils.ErrorUtils.parseErrorMessage
import com.example.reimbifyapp.user.viewmodel.LoginViewModel
import com.example.reimbifyapp.user.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkSession()
        setupActions()
        playAnimation()

        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
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

    private fun checkSession() {
        // TODO: Uncomment when the logout is available
        /*
        lifecycleScope.launch {
            val user = viewModel.getSession().first()
            if (user.isLogin){
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
         */
    }

    private fun setupActions() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (validateInput(email, password)) {
                showLoading(true)
                viewModel.login(email, password)
            }
        }

        // Handle forgot password click
        binding.forgotPasswordTextView.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            showToast("Please enter your email")
            return false
        }

        if (binding.emailEditTextLayout.error != null) {
            showToast("Please enter a valid email address")
            return false
        }

        if (password.isEmpty()) {
            showToast("Password cannot be empty")
            return false
        }

        if (binding.passwordEditTextLayout.error != null) {
            showToast("Password must meet the criteria")
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
        val errorMessage = parseErrorMessage(throwable)
        showToast(errorMessage)
    }

    private fun playAnimation() {
        val imageAnimation = ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }

        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).apply { duration = 200 }
        val message = ObjectAnimator.ofFloat(binding.messageTextView, View.ALPHA, 1f).apply { duration = 200 }
        val emailTv = ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).apply { duration = 200 }
        val emailLayout = ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).apply { duration = 200 }
        val passwordTv = ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).apply { duration = 200 }
        val passwordLayout = ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).apply { duration = 200 }
        val login = ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 1f).apply { duration = 200 }
        val forgotPassword = ObjectAnimator.ofFloat(binding.forgotPasswordTextView, View.ALPHA, 1f).apply { duration = 200 }

        AnimatorSet().apply {
            playSequentially(
                ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, 0f, 0f).apply { duration = 300 },
                title,
                message,
                emailTv,
                emailLayout,
                passwordTv,
                passwordLayout,
                login,
                forgotPassword
            )
            start()
        }

        imageAnimation.start()
    }
}