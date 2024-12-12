package com.example.reimbifyapp.auth.ui

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
import com.example.reimbifyapp.R
import com.example.reimbifyapp.admin.MainActivityAdmin
import com.example.reimbifyapp.auth.factory.UserViewModelFactory
import com.example.reimbifyapp.auth.viewmodel.LoginViewModel
import com.example.reimbifyapp.data.entities.UserSession
import com.example.reimbifyapp.databinding.FragmentLoginBinding
import com.example.reimbifyapp.user.MainActivityUser
import com.example.reimbifyapp.utils.ErrorUtils.parseErrorMessage
import com.example.reimbifyapp.utils.TokenUtils.isTokenValid
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkSessionAndNavigate()
        setupActions()
        observeLoginResult()
        playAnimation()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkSessionAndNavigate() {
        lifecycleScope.launch {
            val userSession = viewModel.getSession().first()
            Log.d("SESSION", "Checking session: $userSession")

            if (userSession.isLogin && isTokenValid(userSession.token)) {
                moveToDashboard(userSession)
            }
        }
    }

    private fun observeLoginResult() {
        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            showLoading(false)

            result.onSuccess { response ->
                lifecycleScope.launch {
                    val newSession = UserSession(
                        userId = response.userId,
                        token = response.accessToken,
                        role = response.role,
                        isLogin = true
                    )
                    viewModel.saveSession(newSession)
                    Log.d("SESSION", "New session saved: $newSession")
                    moveToDashboard(newSession)
                }
            }

            result.onFailure { throwable ->
                handleLoginError(throwable)
            }
        }
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

    private fun moveToDashboard(userSession: UserSession) {
        when (userSession.role) {
            "user", "User" -> {
                showToast("Navigating to User Dashboard")
                val intent = Intent(requireContext(), MainActivityUser::class.java).apply {
                    putExtra("open_fragment", "dashboard")
                }
                startActivity(intent)
                requireActivity().finish()
            }

            "admin", "Admin" -> {
                showToast("Navigating to Admin Dashboard")
                val intent = Intent(requireContext(), MainActivityAdmin::class.java).apply {
                    putExtra("open_fragment", "dashboard")
                }
                startActivity(intent)
                requireActivity().finish()
            }

            else -> {
                showToast("Role not recognized")
            }
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

    private fun showToast(message: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastToastTime > toastDelay) {
            lastToastTime = currentTime
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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