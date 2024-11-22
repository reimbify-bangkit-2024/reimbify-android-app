package com.example.reimbifyapp.user.ui

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.reimbifyapp.databinding.FragmentAddRequestUserBinding
import java.text.SimpleDateFormat
import java.util.*

class AddRequestFragment : Fragment() {

    private var _binding: FragmentAddRequestUserBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null
    private var selectedImageBitmap: Bitmap? = null

    companion object {
        private const val REQUEST_CAMERA = 100
        private const val REQUEST_GALLERY = 200
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRequestUserBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupUI()

        return root
    }

    private fun setupUI() {
        binding.etDate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.btnOpenCamera.setOnClickListener {
            if (Manifest.permission.CAMERA.hasPermission()) {
                openCamera()
            } else {
                Manifest.permission.CAMERA.requestPermission(REQUEST_CAMERA)
            }
        }

        binding.btnUploadGallery.setOnClickListener {
            openGallery()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val formattedDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(selectedDate.time)
                binding.etDate.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun String.hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            this
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun String.requestPermission(requestCode: Int) {
        requestPermissions(arrayOf(this), requestCode)
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, REQUEST_CAMERA)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, REQUEST_GALLERY)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CAMERA -> {
                    val photo = data?.extras?.get("data") as Bitmap
                    selectedImageBitmap = photo
                    displayImage(bitmap = photo, uri = null)
                }
                REQUEST_GALLERY -> {
                    val uri = data?.data
                    selectedImageUri = uri
                    displayImage(bitmap = null, uri = uri)
                }
            }
        }
    }

    private fun displayImage(bitmap: Bitmap?, uri: Uri?) {
        binding.placeholderLayout.visibility = View.GONE
        binding.ivReceipt.apply {
            visibility = View.VISIBLE
            scaleType = ImageView.ScaleType.CENTER_CROP
            if (bitmap != null) {
                setImageBitmap(bitmap)
            } else if (uri != null) {
                setImageURI(uri)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
