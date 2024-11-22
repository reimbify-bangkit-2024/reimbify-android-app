package com.example.reimbifyapp.user.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.reimbifyapp.R
import com.example.reimbifyapp.databinding.FragmentRejectedDetailBinding
import com.example.reimbifyapp.user.data.entities.History

class RejectedDetailFragment : Fragment() {

    private var _binding: FragmentRejectedDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRejectedDetailBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val history = arguments?.getParcelable<History>("history_data")
        history?.let {
            populateDetails(it)
        }
        return root
    }

    @SuppressLint("SetTextI18n")
    private fun populateDetails(history: History) {
        Glide.with(requireContext())
            .load(history.notaImage)
            .placeholder(R.drawable.photo_camera_24dp_48752c_fill0_wght400_grad0_opsz24)
            .into(binding.ivNotaImage)
        binding.etName.setText(history.description)
        binding.etAdminName.setText(history.adminName ?: "")
        binding.etRequestDate.setText(history.receiptDate)
        binding.etDeclineDate.setText(history.declineDate ?: "-")
        binding.etDepartment.setText(history.department)
        binding.etAmount.setText(history.amount.toString())
        binding.tvReasonDecline.text = history.declineReason ?: getString(R.string.default_reason_rejection)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
