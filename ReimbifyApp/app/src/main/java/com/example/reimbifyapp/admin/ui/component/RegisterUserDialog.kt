package com.example.reimbifyapp.admin.ui.component

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.reimbifyapp.R
import com.example.reimbifyapp.admin.factory.UserListViewModelFactory
import com.example.reimbifyapp.admin.viewmodel.UserListViewModel
import com.example.reimbifyapp.auth.ui.component.SuccessDialogFragment
import com.example.reimbifyapp.databinding.DialogRegisterUserBinding
import com.example.reimbifyapp.utils.ErrorUtils.parseErrorMessage
import kotlinx.coroutines.launch

class RegisterUserDialog  : DialogFragment() {

    private var _binding: DialogRegisterUserBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<UserListViewModel> {
        UserListViewModelFactory.getInstance(requireContext())
    }

    private var lastToastTime = 0L
    private val toastDelay = 5000L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRegisterUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchDepartments()
        setupDropdown()
        setupActions()

        viewModel.registerUser.observe(viewLifecycleOwner) { result ->
            showLoading(false)
            result.onSuccess { response ->
                showToast("User registered successfully!")
                showSuccessDialog()
            }
            result.onFailure { throwable ->
                val errorMessage = parseErrorMessage(throwable)
                showToast(errorMessage)
            }
        }
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

    private fun fetchDepartments() {
        viewModel.getAllDepartment()
    }

    private fun setupDropdown() {
        viewModel.allDepartment.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                val departments = response.departments
                val departmentName = departments.map { it.departmentName }

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, departmentName)
                binding.departmentDropdown.setAdapter(adapter)

                binding.departmentDropdown.setOnItemClickListener { _, _, position, _ ->
                    val selectedDepartment = departments[position]
                    Log.d("RegisterDialog", "Selected Department Name: ${selectedDepartment.departmentName}, ID: ${selectedDepartment.departmentId}")
                }

                binding.departmentDropdown.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        val selectedDepartmentName = binding.departmentDropdown.text.toString()
                        val selectedDepartment = departments.find { it.departmentName == selectedDepartmentName }
                        selectedDepartment?.let {
                            Log.d("RegisterDialog", "Selected Department Name: ${it.departmentName}, ID: ${it.departmentId}")
                        } ?: run {
                            Log.e("RegisterDialog", "Invalid Department selected: $selectedDepartmentName")
                        }
                    }
                }
            }.onFailure { throwable ->
                Log.e("AddBankAccountDialog", "Error fetching banks: ${throwable.message}")
                showToast("Failed to load banks. Please try again.")
            }
        }


        val roles = listOf("Admin", "User")
        val roleAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            roles
        )
        binding.roleDropdown.setAdapter(roleAdapter)

        binding.roleDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedRole = roles[position]
            Log.d("RegisterDialog", "Selected Role: $selectedRole")
        }
    }

    private fun setupActions() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val userName = binding.usernameEditText.text.toString()
            val selectedDepartmentName = binding.departmentDropdown.text.toString()
            val role = binding.roleDropdown.text.toString()

            val selectedDepartment = viewModel.allDepartment.value?.getOrNull()?.departments?.find {
                it.departmentName == selectedDepartmentName
            }

            if (validateInput(email, password, userName, selectedDepartment?.departmentId ?: 0, role)) {
                showLoading(true)
                lifecycleScope.launch {
                    try {
                        val departmentId = selectedDepartment?.departmentId
                        if (departmentId != null) {
                            viewModel.registerUser(email, password, userName, departmentId, role)
                        } else {
                            showToast("Invalid department selected")
                        }
                    } catch (e: Exception) {
                        showToast("An error occurred while registering the user")
                        Log.e("RegisterDialog", "Error: ${e.message}", e)
                    }
                }
            }
        }

    }

    private fun validateInput(email: String, password: String, userName: String, departmentId: Int, role: String): Boolean {
        if (email.isEmpty()) {
            showToast("Please input user email")
            return false
        }
        if (binding.emailEditTextLayout.error != null) {
            showToast("Please enter a valid email address")
            return false
        }

        if (password.isEmpty()) {
            showToast("Please input user password")
            return false
        }
        if (binding.passwordEditTextLayout.error != null) {
            showToast("Password must meet the criteria")
            return false
        }

        if (userName.isEmpty()) {
            showToast("Please input user name")
            return false
        }

        if (departmentId < 1) {
            showToast("Please select department")
            return false
        }

        if (role.isEmpty()) {
            showToast("Please select role")
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
            binding.btnRegister.isEnabled = false
            binding.btnRegister.text = getString(R.string.loading)
        } else {
            binding.btnCancel.isEnabled = true
            binding.btnRegister.isEnabled = true
            binding.btnRegister.text = getString(R.string.register)
        }
    }

    private fun showSuccessDialog() {
        val successDialog = SuccessDialogFragment.Companion.newInstance(
            "New User Registered",
            "Success! User has been registered. Please wait for them to activate their account"
        )
        successDialog.show(parentFragmentManager, "SuccessDialog")
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
