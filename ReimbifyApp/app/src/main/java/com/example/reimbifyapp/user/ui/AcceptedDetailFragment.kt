package com.example.reimbifyapp.user.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.reimbifyapp.R
import com.example.reimbifyapp.databinding.FragmentAcceptedDetailBinding
import com.example.reimbifyapp.data.entities.History

class AcceptedDetailFragment : Fragment() {

    private var _binding: FragmentAcceptedDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAcceptedDetailBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val history = arguments?.getParcelable<History>("history_data")
        history?.let { populateDetails(it) }

        return root
    }

    @SuppressLint("SetTextI18n")
    private fun populateDetails(history: History) {
        binding.etName.setText(history.department)
        binding.etAdminName.setText(history.adminName ?: "")
        binding.etAccountNumber.setText(history.accountNumber ?: "")
        binding.etRequestDate.setText(history.receiptDate)
        binding.etReceiveDate.setText(history.receiveDate ?: "")
        binding.etDepartment.setText(history.department)
        binding.etAmount.setText(history.amount.toString())
        binding.tvDescription.text = history.description

        Glide.with(requireContext())
            .load(history.notaImage)
            .placeholder(R.drawable.photo_camera_24dp_48752c_fill0_wght400_grad0_opsz24)
            .into(binding.ivNotaImage)

        Glide.with(requireContext())
            .load(history.transferReceiptImage ?: R.drawable.photo_camera_24dp_48752c_fill0_wght400_grad0_opsz24)
            .placeholder(R.drawable.photo_camera_24dp_48752c_fill0_wght400_grad0_opsz24)
            .into(binding.ivTransferImage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
