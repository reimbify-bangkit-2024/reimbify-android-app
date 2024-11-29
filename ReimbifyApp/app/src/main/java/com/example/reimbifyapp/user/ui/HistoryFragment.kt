package com.example.reimbifyapp.user.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.reimbifyapp.R
import com.example.reimbifyapp.databinding.FragmentHistoryUserBinding
import com.example.reimbifyapp.data.entities.History
import com.example.reimbifyapp.user.ui.adapter.HistoryAdapter

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryUserBinding? = null
    private val binding get() = _binding!!

    private lateinit var rvHistory: RecyclerView
    private val listHistory = ArrayList<History>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryUserBinding.inflate(inflater, container, false)
        val root: View = binding.root

        rvHistory = binding.rvHistory
        rvHistory.layoutManager = LinearLayoutManager(context)
        rvHistory.setHasFixedSize(true)

        // Only populate the list if it is empty
        if (listHistory.isEmpty()) {
            listHistory.addAll(getDummyHistory())
        }
        showRecyclerList()

        return root
    }

    private fun getDummyHistory(): ArrayList<History> {
        return arrayListOf(
            History(
                timestamp = "2024-11-21",
                status = "Under Review",
                receiptDate = "2024-11-20",
                department = "Finance",
                amount = 100000.0,
                description = "Expense reimbursement for November",
                adminName = null,
                accountNumber = null,
                receiveDate = null,
                declineDate = null,
                declineReason = null,
                notaImage = "https://example.com/nota_under_review.jpg",
                transferReceiptImage = null
            ),
            History(
                timestamp = "2024-11-20",
                status = "Approved",
                receiptDate = "2024-11-19",
                department = "HR",
                amount = 50000.0,
                description = "Travel allowance reimbursement",
                adminName = "Admin John",
                accountNumber = "1234567890",
                receiveDate = "2024-11-21",
                declineDate = null,
                declineReason = null,
                notaImage = "https://example.com/nota_approved.jpg",
                transferReceiptImage = "https://example.com/transfer_receipt_approved.jpg"
            ),
            History(
                timestamp = "2024-11-19",
                status = "Rejected",
                receiptDate = "2024-11-18",
                department = "IT",
                amount = 30000.0,
                description = "Equipment purchase reimbursement",
                adminName = "Admin Jane",
                accountNumber = null,
                receiveDate = null,
                declineDate = "2024-11-20",
                declineReason = "Insufficient documentation for the request",
                notaImage = "https://example.com/nota_rejected.jpg",
                transferReceiptImage = null
            )
        )
    }

    private fun showRecyclerList() {
        val adapter = HistoryAdapter(listHistory)
        rvHistory.adapter = adapter

        adapter.setOnItemClickCallback(object : HistoryAdapter.OnItemClickCallback {
            override fun onItemClicked(data: History) {
                when (data.status) {
                    "Under Review" -> navigateToUnderReviewDetail(data)
                    "Approved" -> navigateToAcceptedDetail(data)
                    "Rejected" -> navigateToRejectedDetail(data)
                }
            }
        })
    }

    private fun navigateToUnderReviewDetail(history: History) {
        val bundle = Bundle().apply {
            putParcelable("history_data", history)
        }
        findNavController().navigate(
            R.id.action_navigation_history_to_underReviewDetailFragment,
            bundle
        )
    }

    private fun navigateToAcceptedDetail(history: History) {
        val bundle = Bundle().apply {
            putParcelable("history_data", history)
        }
        findNavController().navigate(
            R.id.action_navigation_history_to_acceptedDetailFragment,
            bundle
        )
    }

    private fun navigateToRejectedDetail(history: History) {
        val bundle = Bundle().apply {
            putParcelable("history_data", history)
        }
        findNavController().navigate(
            R.id.action_navigation_history_to_rejectedDetailFragment,
            bundle
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
