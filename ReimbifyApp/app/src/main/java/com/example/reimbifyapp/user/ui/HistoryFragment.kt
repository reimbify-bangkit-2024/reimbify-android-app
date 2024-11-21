package com.example.reimbifyapp.user.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.reimbifyapp.databinding.FragmentHistoryUserBinding
import com.example.reimbifyapp.user.data.entities.History
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

        listHistory.addAll(getDummyHistory())
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
                description = "Expense reimbursement for November"
            ),
            History(
                timestamp = "2024-11-20",
                status = "Approved",
                receiptDate = "2024-11-19",
                department = "HR",
                amount = 50000.0,
                description = "Travel allowance"
            ),
            History(
                timestamp = "2024-11-19",
                status = "Rejected",
                receiptDate = "2024-11-18",
                department = "IT",
                amount = 30000.0,
                description = "Equipment purchase"
            )
        )
    }

    private fun showRecyclerList() {
        val adapter = HistoryAdapter(listHistory)
        rvHistory.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
