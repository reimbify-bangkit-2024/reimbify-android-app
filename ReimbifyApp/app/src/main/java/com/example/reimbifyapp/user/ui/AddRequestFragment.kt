package com.example.reimbifyapp.user.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.reimbifyapp.databinding.FragmentAddRequestUserBinding
import com.example.reimbifyapp.general.factory.UserViewModelFactory
import com.example.reimbifyapp.user.viewmodel.AddRequestViewModel
import com.example.reimbifyapp.general.viewmodel.LoginViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class AddRequestFragment : Fragment() {

    private var _binding: FragmentAddRequestUserBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null
    private lateinit var viewModel: AddRequestViewModel
    private lateinit var loginViewModel: LoginViewModel

    companion object {
        private const val REQUEST_CAMERA = 100
        private const val REQUEST_GALLERY = 200
        private const val TAG = "AddRequestFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRequestUserBinding.inflate(inflater, container, false)
        val root: View = binding.root
        viewModel = ViewModelProvider(this, UserViewModelFactory.getInstance(requireContext()))[AddRequestViewModel::class.java]
        loginViewModel = ViewModelProvider(this, UserViewModelFactory.getInstance(requireContext()))[LoginViewModel::class.java]

        setupUI()

        viewModel.uploadResponse.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                Toast.makeText(context, "Upload Success", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to upload image.", Toast.LENGTH_SHORT).show()
            }
        }
        return root
    }

    private fun setupUI() {
        binding.btnSubmitRequest.setOnClickListener {
            lifecycleScope.launch {
                try {
                    loginViewModel.getSession().collect { user ->
                        if (user.isLogin) {
                            val authToken = "Bearer ${user.token}"
                            val userId = user.userId
                            selectedImageUri?.let { uri ->
                                try {
                                    viewModel.uploadImage(authToken, uri, userId)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error uploading image", Toast.LENGTH_SHORT).show()
                                }
                            } ?: run {
                                Toast.makeText(context, "Please select an image first.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "User is not logged in.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error retrieving session: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnOpenCamera.setOnClickListener {
            openCamera()
        }

        binding.btnUploadGallery.setOnClickListener {
            openGallery()
        }
    }


    private fun openCamera() {
        val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, REQUEST_CAMERA)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, REQUEST_GALLERY)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CAMERA -> {
                    val photo = data?.extras?.get("data") as? Bitmap
                    if (photo != null) {
                        selectedImageUri = getImageUri(photo)
                        displayImage(uri = selectedImageUri)
                    } else {
                        Toast.makeText(context, "Failed to capture image.", Toast.LENGTH_SHORT).show()
                    }
                }
                REQUEST_GALLERY -> {
                    val uri = data?.data
                    if (uri != null) {
                        selectedImageUri = uri
                        displayImage(uri = uri)
                    } else {
                        Toast.makeText(context, "Failed to select image.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(context, "Failed to get image.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getImageUri(bitmap: Bitmap): Uri {
        val file = File(requireContext().cacheDir, "camera_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return FileProvider.getUriForFile(
            requireContext(),
            "com.example.reimbifyapp.fileprovider",
            file
        )
    }
    private fun displayImage(uri: Uri?) {
        if (uri != null) {
            binding.ivReceipt.setImageURI(uri)
            binding.ivReceipt.visibility = View.VISIBLE
            binding.ivPlaceholderIcon.visibility = View.GONE
        } else {
            Toast.makeText(context, "Failed to load image.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
