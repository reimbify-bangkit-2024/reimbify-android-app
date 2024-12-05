package com.example.reimbifyapp.auth.ui.component

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.reimbifyapp.R
import com.example.reimbifyapp.databinding.DialogChangePasswordBinding
import com.example.reimbifyapp.user.factory.ProfileViewModelFactory
import com.example.reimbifyapp.auth.factory.UserViewModelFactory
import com.example.reimbifyapp.utils.ErrorUtils.parseErrorMessage
import com.example.reimbifyapp.auth.viewmodel.LoginViewModel
import com.example.reimbifyapp.user.viewmodel.ProfileViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ChangePasswordDialogFragment  : DialogFragment() {

    private var _binding: DialogChangePasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: CustomConfirmPasswordEditText

    private val viewModel by viewModels<ProfileViewModel> {
        ProfileViewModelFactory.getInstance(requireContext())
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
        _binding = DialogChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActions()

        viewModel.changePasswordResult.observe(viewLifecycleOwner) { result ->
            showLoading(false)

            result.onSuccess {
                showSuccessDialog()
            }

            result.onFailure { throwable ->
                val errorMessage = parseErrorMessage(throwable)
                showToast(errorMessage)
            }
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
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), R.style.CustomDialogStyle)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
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
                lifecycleScope.launch {
                    val userId = getUserIdFromSession()

                    viewModel.changePassword(userId, oldPassword, newPassword)
                }
            }
        }
    }

    private suspend fun getUserIdFromSession(): String {
        val session = userViewModel.getSession().first()
        return session.userId
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
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastToastTime > toastDelay) {
            lastToastTime = currentTime
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.btnCancel.isEnabled = false
            binding.btnSave.isEnabled = false
            binding.btnSave.text = getString(R.string.loading)
        } else {
            binding.btnCancel.isEnabled = true
            binding.btnSave.isEnabled = true
            binding.btnSave.text = getString(R.string.save)
        }
    }

    private fun showSuccessDialog() {
        val successDialog = SuccessDialogFragment.Companion.newInstance(
            "Password Changed",
            "Your password has been updated successfully!"
        )
        successDialog.show(parentFragmentManager, "SuccessDialog")
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
