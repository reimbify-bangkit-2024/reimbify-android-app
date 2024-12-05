package com.example.reimbifyapp.user.ui.component

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
import com.example.reimbifyapp.auth.factory.UserViewModelFactory
import com.example.reimbifyapp.auth.ui.component.SuccessDialogFragment
import com.example.reimbifyapp.auth.viewmodel.LoginViewModel
import com.example.reimbifyapp.databinding.DialogUpdateBankAccountBinding
import com.example.reimbifyapp.user.factory.ProfileViewModelFactory
import com.example.reimbifyapp.user.viewmodel.ProfileViewModel
import com.example.reimbifyapp.utils.ErrorUtils.parseErrorMessage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UpdateBankAccountDialogFragment  : DialogFragment() {

    private var _binding: DialogUpdateBankAccountBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<ProfileViewModel> {
        ProfileViewModelFactory.getInstance(requireContext())
    }

    private val userViewModel by viewModels<LoginViewModel> {
        UserViewModelFactory.getInstance(requireContext())
    }

    private var accountId: Int = -1
    private var lastToastTime = 0L
    private val toastDelay = 5000L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogUpdateBankAccountBinding.inflate(inflater, container, false)
        accountId = arguments?.getInt("accountId") ?: -1
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (accountId == -1) {
            dismiss()
            showToast("Invalid account ID.")
            return
        }

        viewModel.getAllBank()
        viewModel.getBankAccountById(accountId)

        setupDropdown()
        setupObservers()
        setupActions()
    }

    private fun setupObservers() {
        viewModel.bankAccountById.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                val account = response.account
                binding.accountTitleEditText.setText(account.accountTitle)
                binding.accountNameEditText.setText(account.accountHolderName)
                binding.accountNumberEditText.setText(account.accountNumber)
                binding.bankNameDropdown.setText(account.bank.bankName, false)
            }
            result.onFailure { throwable ->
                val errorMessage = parseErrorMessage(throwable)
                showToast(errorMessage)
            }
        }

        viewModel.allBank.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                val banks = response.banks
                val bankNames = banks.map { it.bankName }

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, bankNames)
                binding.bankNameDropdown.setAdapter(adapter)
            }
            result.onFailure { throwable ->
                val errorMessage = parseErrorMessage(throwable)
                showToast(errorMessage)
            }
        }

        viewModel.updateBankAccount.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                showToast("Bank account updated successfully!")
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

    private fun setupDropdown() {
        binding.bankNameDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedBank = binding.bankNameDropdown.adapter.getItem(position).toString()
            Log.d("AddBankAccountDialog", "Selected Bank: $selectedBank")
        }
    }

    private fun setupActions() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            val title = binding.accountTitleEditText.text.toString().trim()
            val accountName = binding.accountNameEditText.text.toString().trim()
            val accountNumber = binding.accountNumberEditText.text.toString().trim()
            val selectedBankName = binding.bankNameDropdown.text.toString().trim()

            if (validateInput(title, accountName, accountNumber, selectedBankName)) {
                val selectedBank = viewModel.allBank.value?.getOrNull()?.banks?.find { it.bankName == selectedBankName }

                if (selectedBank != null) {
                    lifecycleScope.launch {
                        saveBankAccount(title, accountName, accountNumber, selectedBank.bankId)
                    }
                } else {
                    showToast("Invalid bank selected. Please try again.")
                }
            }
        }
    }

    private fun validateInput(title: String, accountName: String, accountNumber: String, bankName: String): Boolean {
        if (bankName.isEmpty()) {
            showToast("Please select a bank")
            return false
        }
        if (accountNumber.isEmpty()) {
            showToast("Please enter an account number")
            return false
        }
        if (accountName.isEmpty()) {
            showToast("Please enter an account name")
            return false
        }
        if (title.isEmpty()) {
            showToast("Please enter account title")
            return false
        }
        return true
    }

    private suspend fun saveBankAccount(title: String, name: String, number: String, bankId: Int) {
        try {
            showLoading(true)
            val userId = userViewModel.getSession().first().userId.toInt()
            Log.d("SAVE_BANK_ACCOUNT", "Title: $title, Name: $name, Number: $number, BankId: $bankId, UserId: $userId")

            viewModel.updateBankAccount(accountId, title, name, number, bankId)
        } catch (e: Exception) {
            showToast("Failed to update bank account. Please try again.")
        } finally {
            showLoading(false)
        }
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
        showLoading(false)
        val successDialog = SuccessDialogFragment.Companion.newInstance(
            "Bank Account Updated",
            "Your new bank account has been updates successfully!"
        )

        parentFragmentManager.setFragmentResult("bank_account_updated", Bundle())

        successDialog.show(parentFragmentManager, "SuccessDialog")
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(accountId: Int): UpdateBankAccountDialogFragment {
            val fragment = UpdateBankAccountDialogFragment()
            val args = Bundle()
            args.putInt("accountId", accountId)
            fragment.arguments = args
            return fragment
        }
    }
}
