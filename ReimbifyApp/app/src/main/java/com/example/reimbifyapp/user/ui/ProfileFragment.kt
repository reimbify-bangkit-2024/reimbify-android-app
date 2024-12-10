package com.example.reimbifyapp.user.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.reimbifyapp.R
import com.example.reimbifyapp.databinding.FragmentProfileUserBinding
import com.example.reimbifyapp.data.entities.User
import com.example.reimbifyapp.user.factory.ProfileViewModelFactory
import com.example.reimbifyapp.auth.factory.UserViewModelFactory
import com.example.reimbifyapp.auth.ui.AuthActivity
import com.example.reimbifyapp.auth.ui.component.ChangePasswordDialogFragment
import com.example.reimbifyapp.auth.viewmodel.LoginViewModel
import com.example.reimbifyapp.data.entities.Account
import com.example.reimbifyapp.user.ui.AddRequestFragment.Companion
import com.example.reimbifyapp.user.ui.adapter.BankAccountAdapter
import com.example.reimbifyapp.user.ui.component.AddBankAccountDialogFragment
import com.example.reimbifyapp.user.ui.component.UpdateBankAccountDialogFragment
import com.example.reimbifyapp.user.viewmodel.ProfileViewModel
import com.example.reimbifyapp.utils.ErrorUtils.parseErrorMessage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class ProfileFragment : Fragment(R.layout.fragment_profile_user) {

    private var _binding: FragmentProfileUserBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null
    private var userId: Int? = null
    private lateinit var bankAccountAdapter: BankAccountAdapter

    companion object{
        private const val REQUEST_GALLERY = 200
    }

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

        parentFragmentManager.setFragmentResultListener("bank_account_deleted", viewLifecycleOwner) { _, _ ->
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

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    private fun setupObservers() {
        viewModel.getUserResult.observe(viewLifecycleOwner) { result ->
            showLoading(false)
            result.onSuccess { user ->
                displayUserData(user.users[0])
                fetchUserBankAccounts(user.users[0].userId.toInt())
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

        viewModel.uploadResponse.observe(viewLifecycleOwner) { response ->
            response?.let {
                if (it.success) {
                    showToast("Profile picture uploaded successfully!")
                    fetchUserProfile()
                } else {
                    showToast("Failed to upload profile picture: ${it.message}")
                }
            } ?: showToast("Upload failed: Unknown error")
        }
    }

    private fun uploadProfilePicture() {
        selectedImageUri?.let { uri ->
            lifecycleScope.launch {
                try {
                    val userSession = userViewModel.getSession().first()
                    viewModel.UploadImage(uri, userSession.userId)
                } catch (e: Exception) {
                    showToast("Failed to upload profile picture: ${e.localizedMessage}")
                }
            }
        } ?: showToast("Please select an image first")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GALLERY && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedImageUri = uri
                binding.profilePicture.setImageURI(uri)
                uploadProfilePicture()
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
                val errorMessage = parseErrorMessage(e)
                showToast("Failed to load bank accounts: $errorMessage")
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
        val profileImageUrl = user.profileImageUrl
        Log.d("ProfileFragment", "Profile image URL: $profileImageUrl")
        profileImageUrl?.let { url ->
            Glide.with(requireContext())
                .load(url)
                .placeholder(R.drawable.baseline_account_circle_24)
                .error(R.drawable.baseline_account_circle_24)
                .into(binding.profilePicture)
                .onLoadFailed(null)
        } ?: run {
            Log.e("ProfileFragment", "Profile image URL is null")
        }
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

        binding.profilePicture.setOnClickListener {
            openGallery()
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
}