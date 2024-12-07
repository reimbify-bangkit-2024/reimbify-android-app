package com.example.reimbifyapp.admin.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.reimbifyapp.R
import com.example.reimbifyapp.admin.viewmodel.DashboardViewModel
import com.example.reimbifyapp.admin.factory.DashboardViewModelFactory
import com.example.reimbifyapp.databinding.FragmentDashboardAdminBinding
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.example.reimbifyapp.data.network.response.GetHistoryAllUserResponse
import com.example.reimbifyapp.data.network.response.StatusResponse
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.Calendar

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
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val status = "approved"
        dashboardViewModel.getMonthlyAmount(year, status).observe(viewLifecycleOwner) { listHistoryResponse ->
            listHistoryResponse?.let { response ->
                updateLineChart(response.histories)
            }
        }

        dashboardViewModel.getTotalRequestStatus().observe(viewLifecycleOwner) { statusResponse ->
            updatePieChart(statusResponse)
        }

        return binding.root
    }

    private fun updatePieChart(statusResponse: StatusResponse) {
        val pieEntries = mutableListOf<PieEntry>()
        pieEntries.add(PieEntry(statusResponse.approved.toFloat(), "Approved"))
        pieEntries.add(PieEntry(statusResponse.under_review.toFloat(), "Under Review"))
        pieEntries.add(PieEntry(statusResponse.rejected.toFloat(), "Rejected"))
        val pieDataSet = com.github.mikephil.charting.data.PieDataSet(pieEntries, "").apply {
            colors = listOf(
                ContextCompat.getColor(requireContext(), R.color.green_500),
                ContextCompat.getColor(requireContext(), R.color.purple_500),
                ContextCompat.getColor(requireContext(), R.color.red_500)
            )
            valueTextSize = 12f
            setDrawValues(true)
            sliceSpace = 3f
            setValueTextColor(Color.WHITE)
        }

        val pieData = com.github.mikephil.charting.data.PieData(pieDataSet)
        binding.pieChart.apply {
            data = pieData
            description.isEnabled = false
            legend.isEnabled = true
            setEntryLabelColor(Color.WHITE)
            invalidate()
        }
    }

    private fun updateLineChart(histories: List<GetHistoryAllUserResponse>) {
        val entries = mutableListOf<Entry>()
        val dataSets = mutableListOf<ILineDataSet>()
        val approvedEntries = histories.mapIndexed { index, history ->
            Entry(index.toFloat(), history.status.approved.toFloat())
        }

        val approvedDataSet = LineDataSet(approvedEntries, "Approved Amount").apply {
            color = android.graphics.Color.GREEN
            setCircleColor(android.graphics.Color.GREEN)
            setDrawValues(false)
        }
        dataSets.add(approvedDataSet)
        val approvedAmounts = histories.map { it.status.approved }
        val min = approvedAmounts.minOrNull() ?: 0.0
        val max = approvedAmounts.maxOrNull() ?: 0.0
        val q1 = approvedAmounts.sorted().let { it[it.size / 4] }
        val q2 = approvedAmounts.sorted().let { it[it.size / 2] }
        val q3 = approvedAmounts.sorted().let { it[3 * it.size / 4] }
        val lineData = LineData(dataSets)
        binding.lineChart.apply {
            data = lineData
            description.isEnabled = false

            xAxis.apply {
                granularity = 1f
                isGranularityEnabled = true
                valueFormatter = MonthAxisValueFormatter(histories.map { it.month })
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                yOffset = 10f
            }
            axisRight.isEnabled = false
            axisLeft.apply {
                axisMinimum = min.toFloat()
                axisMaximum = max.toFloat()
                setDrawLabels(true)
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return formatCurrency(value.toDouble())
                    }
                }
                setLabelCount(5, true)
                axisMinimum = min.toFloat()
                axisMaximum = max.toFloat()
            }

            invalidate()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatCurrency(amount: Double): String {
        val formattedAmount = when {
            amount >= 1_000_000_000.0 -> {
                val billions = amount / 1_000_000_000.0
                String.format("%.1f M", billions)  // "M" untuk Miliar
            }
            amount >= 1_000_000.0 -> {
                val millions = amount / 1_000_000.0
                String.format("%.1f jt", millions)  // "jt" untuk Juta
            }
            else -> String.format("%,.0f", amount)
        }
        return "Rp $formattedAmount"
    }

    inner class MonthAxisValueFormatter(private val months: List<Int>) : com.github.mikephil.charting.formatter.ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val index = value.toInt()
            return if (index >= 0 && index < months.size) {
                getMonthName(months[index])
            } else {
                ""
            }
        }

        private fun getMonthName(monthNumber: Int): String {
            return when (monthNumber) {
                1 -> "Jan"
                2 -> "Feb"
                3 -> "Mar"
                4 -> "Apr"
                5 -> "May"
                6 -> "Jun"
                7 -> "Jul"
                8 -> "Aug"
                9 -> "Sep"
                10 -> "Oct"
                11 -> "Nov"
                12 -> "Dec"
                else -> ""
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
