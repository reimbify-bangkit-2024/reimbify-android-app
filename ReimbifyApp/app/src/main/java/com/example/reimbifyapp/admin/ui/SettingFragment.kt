package com.example.reimbifyapp.admin.ui

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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.reimbifyapp.R
import com.example.reimbifyapp.admin.factory.SettingViewModelFactory
import com.example.reimbifyapp.admin.viewmodel.SettingViewModel
import com.example.reimbifyapp.auth.factory.UserViewModelFactory
import com.example.reimbifyapp.auth.ui.AuthActivity
import com.example.reimbifyapp.auth.viewmodel.LoginViewModel
import com.example.reimbifyapp.data.entities.User
import com.example.reimbifyapp.databinding.FragmentSettingAdminBinding
import com.example.reimbifyapp.auth.ui.component.ChangePasswordDialogFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.getValue

class SettingFragment : Fragment() {

    companion object {
        const val REQUEST_GALLERY = 200
    }

    private var _binding: FragmentSettingAdminBinding? = null
    private val binding get() = _binding!!
    private var isUploading = false
    private var isShowingUploadToast = false
    private var lastImageSelectionTime = 0L
    private val IMAGE_SELECTION_DELAY = 1000L
    private var selectedImageUri: Uri? = null
    private val settingViewModel by viewModels<SettingViewModel> {
        SettingViewModelFactory.getInstance(requireContext())
    }

    private val userViewModel by viewModels<LoginViewModel> {
        UserViewModelFactory.getInstance(requireContext())
    }

    private var lastToastTime = 0L
    private val toastDelay = 5000L
    private var isLoadingProfile = false

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
        setupThemeSwitchListener()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    private fun setupObservers() {
        settingViewModel.getUserResult.observe(viewLifecycleOwner) { result ->
            isLoadingProfile = false
            showLoading(false)
            result.onSuccess { user ->
                displayUserData(user.users[0])
            }
            result.onFailure { throwable ->
                showToast("Failed to load user profile: ${throwable.localizedMessage}")
            }
        }

        settingViewModel.getThemeSettings().observe(viewLifecycleOwner) { isDarkModeActive ->
            if (binding.switchTheme.isChecked != isDarkModeActive) {
                binding.switchTheme.setOnCheckedChangeListener(null)
                binding.switchTheme.isChecked = isDarkModeActive
                setupThemeSwitchListener()
            }
        }

        settingViewModel.uploadResponse.observe(viewLifecycleOwner) { response ->
            isUploading = false
            isShowingUploadToast = false

            response?.let {
                if (it.success) {
                    fetchUserProfile()
                } else {
                    showToast("Failed to upload profile picture: ${it.message}")
                }
            } ?: showToast("Upload failed: Unknown error")
        }
    }

    private fun fetchUserProfile() {
        if (isLoadingProfile) return

        isLoadingProfile = true
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
            } finally {
                isLoadingProfile = false
            }
        }
    }

    private fun uploadProfilePicture() {
        if (isUploading || isShowingUploadToast) {
            return
        }

        selectedImageUri?.let { uri ->
            isUploading = true
            isShowingUploadToast = true

            lifecycleScope.launch {
                try {
                    val userSession = userViewModel.getSession().first()
                    settingViewModel.UploadImage(uri, userSession.userId)
                } catch (e: Exception) {
                    showToast("Failed to upload profile picture: ${e.localizedMessage}")
                    isUploading = false
                    isShowingUploadToast = false
                }
            }
        } ?: showToast("Please select an image first")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val currentTime = System.currentTimeMillis()

        if (requestCode == SettingFragment.REQUEST_GALLERY && resultCode == Activity.RESULT_OK) {
            if (currentTime - lastImageSelectionTime > IMAGE_SELECTION_DELAY) {
                data?.data?.let { uri ->
                    selectedImageUri = uri
                    binding.profilePicture.setImageURI(uri)
                    uploadProfilePicture()
                    lastImageSelectionTime = currentTime
                }
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
        val profileImageUrl = user.profileImageUrl
        binding.profilePicture.setImageResource(R.drawable.baseline_account_circle_24)
        Log.d("ProfileFragment", "Profile image URL: $profileImageUrl")
        profileImageUrl?.let { url ->
            Glide.with(requireContext())
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .circleCrop()
                .placeholder(R.drawable.baseline_account_circle_24)
                .error(R.drawable.baseline_account_circle_24)
                .into(binding.profilePicture)
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

        binding.profilePicture.setOnClickListener {
            openGallery()
        }
    }

    private fun setupThemeSwitchListener() {
        binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
            settingViewModel.saveThemeSetting(isChecked)

            requireActivity().apply {
                val intent = intent.apply {
                    putExtra("open_fragment", "setting")
                }
                finish()
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
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
        if (currentTime - lastToastTime > toastDelay && isAdded) {
            lastToastTime = currentTime
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}