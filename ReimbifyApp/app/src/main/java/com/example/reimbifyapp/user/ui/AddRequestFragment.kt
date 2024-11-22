package com.example.reimbifyapp.user.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.reimbifyapp.databinding.FragmentAddRequestUserBinding
import java.text.SimpleDateFormat
import java.util.*

class AddRequestFragment : Fragment() {

    private var _binding: FragmentAddRequestUserBinding? = null
    private val binding get() = _binding!!

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
            Toast.makeText(context, "Open Camera Clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnUploadGallery.setOnClickListener {
            Toast.makeText(context, "Upload from Gallery Clicked", Toast.LENGTH_SHORT).show()
        }

        binding.flReceipt.setOnClickListener {
            Toast.makeText(context, "Receipt Frame Clicked", Toast.LENGTH_SHORT).show()
        }

        updateImageStatus(isValid = true)
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

    private fun updateImageStatus(isValid: Boolean) {
        if (isValid) {
            binding.llImageStatus.visibility = View.VISIBLE
        } else {
            binding.llImageStatus.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
