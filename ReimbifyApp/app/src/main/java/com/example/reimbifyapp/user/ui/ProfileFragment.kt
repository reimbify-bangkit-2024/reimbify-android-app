package com.example.reimbifyapp.user.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.reimbifyapp.R
import com.example.reimbifyapp.databinding.FragmentProfileUserBinding
import com.example.reimbifyapp.user.data.entities.User
import com.example.reimbifyapp.user.data.entities.UserSession
import com.example.reimbifyapp.user.factory.ProfileViewModelFactory
import com.example.reimbifyapp.user.factory.UserViewModelFactory
import com.example.reimbifyapp.user.ui.component.ChangePasswordDialogFragment
import com.example.reimbifyapp.user.viewmodel.LoginViewModel
import com.example.reimbifyapp.user.viewmodel.ProfileViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile_user) {

    private var _binding: FragmentProfileUserBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<ProfileViewModel> {
        ProfileViewModelFactory.getInstance(requireContext())
    }

    private val userViewModel by viewModels<LoginViewModel> {
        UserViewModelFactory.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchUserProfile()
        setupActions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

                viewModel.getUserResult.observe(viewLifecycleOwner) { result ->
                    showLoading(false)
                    result.onSuccess { user ->
                        displayUserData(user.user)
                    }
                }


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
        binding.tvProfileDepartment.text = user.department.departmentName ?: "-"
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
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingOverlay.visibility = View.VISIBLE
        } else {
            binding.loadingOverlay.visibility = View.GONE
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}