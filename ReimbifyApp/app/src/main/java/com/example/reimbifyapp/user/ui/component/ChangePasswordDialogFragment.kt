package com.example.reimbifyapp.user.ui.component

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.reimbifyapp.R
import com.example.reimbifyapp.databinding.DialogChangePasswordBinding
import com.example.reimbifyapp.user.factory.ProfileViewModelFactory
import com.example.reimbifyapp.user.factory.UserViewModelFactory
import com.example.reimbifyapp.user.viewmodel.LoginViewModel
import com.example.reimbifyapp.user.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

class ChangePasswordDialogFragment  : DialogFragment() {

    private var _binding: DialogChangePasswordBinding? = null
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
        _binding = DialogChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActions()

        viewModel.changePasswordResult.observe(viewLifecycleOwner) { result ->
            showLoading(false)

            result.onSuccess {
                showToast("Password changed successfully!")
                dismiss()
            }

            result.onFailure { throwable ->
                showToast("Failed to change password: ${throwable.localizedMessage}")
            }
        }
    }

    private fun setupActions() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            val oldPassword = binding.prevPasswordEditText.text.toString().trim()
            val newPassword = binding.newPasswordEditText.text.toString().trim()
            val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()

            if (validateInput(oldPassword, newPassword, confirmPassword)) {
                showLoading(true)
                getUserIdFromSession()?.let { it1 -> viewModel.changePassword(userId = it1, oldPassword, newPassword) }
            }
        }
    }

    private fun getUserIdFromSession(): String? {
        var userId: String? = null
        lifecycleScope.launch {
            userViewModel.getSession().collect { session ->
                userId = session.userId
            }
        }
        return userId
    }

    private fun validateInput(oldPassword: String, newPassword: String, confirmPassword: String): Boolean {
        if (oldPassword.isEmpty()) {
            showToast("Enter your old password")
            return false
        }
        if (newPassword.isEmpty()) {
            showToast("Enter your new password")
            return false
        }
        if (confirmPassword.isEmpty() || confirmPassword != newPassword) {
            showToast("Passwords do not match")
            return false
        }
        return true
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.btnSave.isEnabled = false
            binding.btnSave.text = getString(R.string.loading)
        } else {
            binding.btnSave.isEnabled = true
            binding.btnSave.text = getString(R.string.save)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
