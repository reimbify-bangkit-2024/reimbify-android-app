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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reimbifyapp.R
import com.example.reimbifyapp.databinding.FragmentProfileUserBinding
import com.example.reimbifyapp.data.entities.User
import com.example.reimbifyapp.user.factory.ProfileViewModelFactory
import com.example.reimbifyapp.auth.factory.UserViewModelFactory
import com.example.reimbifyapp.auth.ui.AuthActivity
import com.example.reimbifyapp.user.ui.component.ChangePasswordDialogFragment
import com.example.reimbifyapp.auth.viewmodel.LoginViewModel
import com.example.reimbifyapp.data.entities.Account
import com.example.reimbifyapp.user.ui.adapter.BankAccountAdapter
import com.example.reimbifyapp.user.ui.component.AddBankAccountDialogFragment
import com.example.reimbifyapp.user.ui.component.UpdateBankAccountDialogFragment
import com.example.reimbifyapp.user.viewmodel.ProfileViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile_user) {

    private var _binding: FragmentProfileUserBinding? = null
    private val binding get() = _binding!!

    private lateinit var bankAccountAdapter: BankAccountAdapter

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

        parentFragmentManager.setFragmentResultListener("bank_account_added", viewLifecycleOwner) { _, _ ->
            lifecycleScope.launch {
                val userId = userViewModel.getSession().first().userId.toInt()
                viewModel.getBankAccountByUserId(userId)
            }
        }

        parentFragmentManager.setFragmentResultListener("bank_account_updated", viewLifecycleOwner) { _, _ ->
            lifecycleScope.launch {
                val userId = userViewModel.getSession().first().userId.toInt()
                viewModel.getBankAccountByUserId(userId)
            }
        }

        bankAccountAdapter = BankAccountAdapter(mutableListOf()) { account ->
            val dialog = UpdateBankAccountDialogFragment.newInstance(account.accountId)
            dialog.show(parentFragmentManager, "UpdateBankAccountDialog")
        }

        binding.recyclerViewBankAccounts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewBankAccounts.adapter = bankAccountAdapter

        setupObservers()
        fetchUserProfile()
        setupActions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupObservers() {
        viewModel.getUserResult.observe(viewLifecycleOwner) { result ->
            showLoading(false)
            result.onSuccess { user ->
                displayUserData(user.user)
                fetchUserBankAccounts(user.user.userId.toInt())
            }
            result.onFailure { throwable ->
                showToast("Failed to load user profile: ${throwable.localizedMessage}")
            }
        }

        viewModel.bankAccountUserId.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                displayBankAccounts(response.accounts)
                response.accounts.forEach {
                    println("Fetched Account: ${it.accountTitle}, ${it.accountNumber}")
                }
            }
            result.onFailure { throwable ->
                showToast("Failed to load bank accounts: ${throwable.localizedMessage}")
            }
        }

        viewModel.createBankAccount.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                lifecycleScope.launch {
                    val userId = userViewModel.getSession().first().userId.toInt()
                    viewModel.getBankAccountByUserId(userId)
                    showToast("Bank account added successfully!")
                }
            }
            result.onFailure { throwable ->
                showToast("Failed to add bank account: ${throwable.localizedMessage}")
            }
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

                viewModel.getUser(userSession.userId)
            } catch (e: Exception) {
                showLoading(false)
                showToast("Failed to load user profile: ${e.localizedMessage}")
            }
        }
    }

    private fun fetchUserBankAccounts(userId: Int) {
        lifecycleScope.launch {
            try {
                viewModel.getBankAccountByUserId(userId)
            } catch (e: Exception) {
                showToast("Failed to load bank accounts: ${e.localizedMessage}")
            }
        }
    }

    private fun displayBankAccounts(accounts: List<Account>) {
        println("Updating RecyclerView with ${accounts.size} accounts")
        bankAccountAdapter.updateAccounts(accounts)
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

        binding.ivAddBankAccount.setOnClickListener {
            lifecycleScope.launch {
                val dialog = AddBankAccountDialogFragment()
                dialog.show(parentFragmentManager, "AddBankAccountDialog")
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
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}