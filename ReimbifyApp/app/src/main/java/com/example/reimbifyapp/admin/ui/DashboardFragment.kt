package com.example.reimbifyapp.admin.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.reimbifyapp.admin.viewmodel.DashboardViewModel
import com.example.reimbifyapp.databinding.FragmentDashboardAdminBinding
import com.example.reimbifyapp.admin.factory.DashboardViewModelFactory

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardAdminBinding? = null
    private val binding get() = _binding!!
    private lateinit var dashboardViewModel: DashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardAdminBinding.inflate(inflater, container, false)

        dashboardViewModel = ViewModelProvider(
            this,
            DashboardViewModelFactory.getInstance(requireContext())
        )[DashboardViewModel::class.java]

        dashboardViewModel.getAmount("approved,under_review").observe(viewLifecycleOwner) { amountResponse ->
            binding.tvAmountApproved.text = formatCurrency(amountResponse.approvedAmount)
            binding.tvAmountPending.text = formatCurrency(amountResponse.pendingAmount)
        }

        return binding.root
    }

    @SuppressLint("DefaultLocale")
    private fun formatCurrency(amount: Double): String {
        val formattedAmount = when {
            amount >= 1_000_000_000_000.0 -> {
                val trillions = amount / 1_000_000_000_000.0
                String.format("%.4f T", trillions)
            }
            amount >= 1_000_000_000.0 -> {
                val billions = amount / 1_000_000_000.0
                String.format("%.4f M", billions)
            }
            else -> String.format("%,.0f", amount)
        }
        return "Rp $formattedAmount"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}