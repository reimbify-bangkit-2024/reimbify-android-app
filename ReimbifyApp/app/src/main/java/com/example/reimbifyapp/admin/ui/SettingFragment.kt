package com.example.reimbifyapp.admin.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.reimbifyapp.admin.factory.SettingViewModelFactory
import com.example.reimbifyapp.admin.viewmodel.SettingViewModel
import com.example.reimbifyapp.auth.factory.UserViewModelFactory
import com.example.reimbifyapp.auth.ui.AuthActivity
import com.example.reimbifyapp.auth.viewmodel.LoginViewModel
import com.example.reimbifyapp.data.entities.User
import com.example.reimbifyapp.databinding.FragmentSettingAdminBinding
import com.example.reimbifyapp.auth.ui.component.ChangePasswordDialogFragment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.getValue

class SettingFragment : Fragment() {
    private var _binding: FragmentSettingAdminBinding? = null
    private val binding get() = _binding!!

    private val settingViewModel by viewModels<SettingViewModel> {
        SettingViewModelFactory.getInstance(requireContext())
    }

    private val userViewModel by viewModels<LoginViewModel> {
        UserViewModelFactory.getInstance(requireContext())
    }

    private var lastToastTime = 0L
    private val toastDelay = 5000L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        fetchUserProfile()
        setupActions()
    }

    private fun setupObservers() {
        settingViewModel.getUserResult.observe(viewLifecycleOwner) { result ->
            showLoading(false)
            result.onSuccess { user ->
                displayUserData(user.users[0])
            }
            result.onFailure { throwable ->
                showToast("Failed to load user profile: ${throwable.localizedMessage}")
            }
        }

        settingViewModel.getThemeSettings().observe(viewLifecycleOwner) { isDarkModeActive ->
            binding.switchTheme.isChecked = isDarkModeActive
        }
    }

    private fun fetchUserProfile() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val userSession = userViewModel.getSession().first()

                if (userSession.token.isEmpty()) {
                    userViewModel.logout()
                    showToast("Session expired. Please log in again.")
                    navigateToAuthActivity()
                    return@launch
                }

                settingViewModel.getUser(userSession.userId)
            } catch (e: Exception) {
                showLoading(false)
                showToast("Failed to load user profile: ${e.localizedMessage}")
            }
        }
    }

    private fun navigateToAuthActivity() {
        val intent = Intent(requireContext(), AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    private fun displayUserData(user: User) {
        binding.tvProfileName.text = user.userName
        binding.tvProfileDepartment.text = user.department.departmentName
        binding.tvEmail.text = user.email
        binding.tvRole.text = user.role
    }

    private fun setupActions() {
        binding.btnChangePassword.setOnClickListener {
            val dialog = ChangePasswordDialogFragment()
            dialog.show(parentFragmentManager, "ChangePasswordDialog")
        }

        binding.btnSignOut.setOnClickListener {
            lifecycleScope.launch {
                userViewModel.logout()
                showToast("Signed out successfully!")
                navigateToAuthActivity()
            }
        }

        binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
            settingViewModel.saveThemeSetting(isChecked)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
