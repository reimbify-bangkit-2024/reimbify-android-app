package com.example.reimbifyapp.admin.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.reimbifyapp.R
import com.example.reimbifyapp.admin.factory.DashboardViewModelFactory
import com.example.reimbifyapp.admin.ui.adapter.RequestAdapter
import com.example.reimbifyapp.admin.viewmodel.DashboardViewModel
import com.example.reimbifyapp.auth.factory.UserViewModelFactory
import com.example.reimbifyapp.auth.viewmodel.LoginViewModel
import com.example.reimbifyapp.data.network.response.DepartmentRequest
import com.example.reimbifyapp.data.network.response.GetHistoryAllUserResponse
import com.example.reimbifyapp.data.network.response.StatusResponse
import com.example.reimbifyapp.databinding.FragmentDashboardAdminBinding
import com.example.reimbifyapp.user.factory.ProfileViewModelFactory
import com.example.reimbifyapp.user.viewmodel.ProfileViewModel
import com.example.reimbifyapp.utils.ErrorUtils.parseErrorMessage
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.getValue

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardAdminBinding? = null
    private val binding get() = _binding!!
    private lateinit var dashboardViewModel: DashboardViewModel
    private var isFetchingUser = false
    private var lastToastTime = 0L
    private val toastDelay = 5000L

    private val userViewModel by viewModels<LoginViewModel> {
        UserViewModelFactory.getInstance(requireContext())
    }

    private val profileViewModel by viewModels<ProfileViewModel> {
        ProfileViewModelFactory.getInstance(requireContext())
    }

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

        dashboardViewModel.getTotalRequestByDepartment("approved").observe(viewLifecycleOwner) { departmentData ->
            Log.d("DashboardFragment", "Department Data: $departmentData")
            updateBarChart(departmentData)
        }

        profileViewModel.getUserResult.observe(viewLifecycleOwner) { result ->
            if (!isAdded) return@observe
            isFetchingUser = false

            result.onSuccess { user ->
                setUserName(user.users[0].userName)
            }
            result.onFailure { throwable ->
                println("Failed to load user profile: ${throwable.localizedMessage}")
                showToast("Failed to load user profile: ${throwable.localizedMessage}")
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchUserIdAndLoadUser()
    }

    private fun fetchUserIdAndLoadUser() {
        isFetchingUser = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val session = userViewModel.getSession().first()
                val userId = session.userId

                profileViewModel.getUser(userId)
            } catch (e: Exception) {
                val errorMessage = parseErrorMessage(e)
                showToast("Failed to fetch session or user details: $errorMessage")
            } finally {
                isFetchingUser = false
            }
        }
    }

    private fun setUserName(userName: String) {
        if (userName.isNotEmpty()) {
            binding.userGreeting.text = getString(R.string.greeting, userName)
        } else {
            _binding?.userGreeting?.text = getString(R.string.greeting, getString(R.string.default_user_name))
        }
    }

    private fun updateBarChart(departmentData: List<DepartmentRequest>) {
        if (departmentData.isEmpty()) {
            binding.tvNoRequests.visibility = View.VISIBLE
            binding.barChart.visibility = View.GONE
            return
        }

        binding.tvNoRequests.visibility = View.GONE
        binding.barChart.visibility = View.VISIBLE
        val entries = departmentData.mapIndexed { index, data ->
            BarEntry(index.toFloat(), data.total.toFloat())
        }
        val labels = departmentData.map { it.departmentName }
        val barDataSet = BarDataSet(entries, "Jumlah Request per Departemen").apply {
            color = ContextCompat.getColor(requireContext(), R.color.green_500)
            setDrawValues(true)
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_color_setting)
            valueTextSize = 10f

            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }
        val barData = BarData(barDataSet)
        val maxValue = departmentData.maxOfOrNull { it.total }?.toFloat() ?: 0f

        binding.barChart.apply {
            data = barData
            description.isEnabled = false
            legend.isEnabled = false
            setExtraOffsets(0f, 20f, 0f, 20f)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                isGranularityEnabled = true
                setDrawGridLines(false)
                valueFormatter = IndexAxisValueFormatter(labels)
                labelCount = labels.size
                textColor = ContextCompat.getColor(requireContext(), R.color.text_color_setting)
                labelRotationAngle = 0f
                setDrawAxisLine(true)
                textSize = 12f
                yOffset = 10f
                axisMinimum = -0.5f
                axisMaximum = departmentData.size.toFloat() - 0.5f
            }
            axisLeft.apply {
                setDrawGridLines(true)
                setDrawLabels(true)
                textColor = ContextCompat.getColor(requireContext(), R.color.text_color_setting)
                setDrawZeroLine(true)
                zeroLineColor = ContextCompat.getColor(requireContext(), R.color.text_color_setting)
                axisMinimum = 0f
                labelCount = 5
                setDrawLabels(true)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return value.toInt().toString()
                    }
                }
                axisMaximum = maxValue * 1.1f
            }
            axisRight.isEnabled = false
            animateY(500)
            invalidate()
        }
    }

    private fun updatePieChart(statusResponse: StatusResponse) {
        val total = statusResponse.approved + statusResponse.underReview + statusResponse.rejected

        val approvedPercentage = (statusResponse.approved / total.toFloat()) * 100
        val underReviewPercentage = (statusResponse.underReview / total.toFloat()) * 100
        val rejectedPercentage = (statusResponse.rejected / total.toFloat()) * 100

        val pieEntries = mutableListOf<PieEntry>()
        pieEntries.add(PieEntry(approvedPercentage, "Approved"))
        pieEntries.add(PieEntry(underReviewPercentage, "Under Review"))
        pieEntries.add(PieEntry(rejectedPercentage, "Rejected"))

        val pieDataSet = com.github.mikephil.charting.data.PieDataSet(pieEntries, "").apply {
            colors = listOf(
                ContextCompat.getColor(requireContext(), R.color.green_500),
                ContextCompat.getColor(requireContext(), R.color.purple_500),
                ContextCompat.getColor(requireContext(), R.color.red_500)
            )
            valueTextSize = 12f
            setDrawValues(true)
            sliceSpace = 3f
            setValueTextColor(ContextCompat.getColor(requireContext(), R.color.text_color_setting))

            valueFormatter = object : ValueFormatter() {
                @SuppressLint("DefaultLocale")
                override fun getFormattedValue(value: Float): String {
                    return String.format("%.1f%%", value)
                }
            }
        }

        val pieData = com.github.mikephil.charting.data.PieData(pieDataSet)

        binding.pieChart.apply {
            data = pieData
            description.isEnabled = false
            legend.isEnabled = true
            legend.textColor = ContextCompat.getColor(requireContext(), R.color.text_color_setting)
            setEntryLabelColor(ContextCompat.getColor(requireContext(), R.color.text_color_setting))
            invalidate()
        }
    }

    private fun updateLineChart(histories: List<GetHistoryAllUserResponse>) {
        val dataSets = mutableListOf<ILineDataSet>()
        val approvedEntries = histories.mapIndexed { index, history ->
            Entry(index.toFloat(), history.status.approved.toFloat())
        }

        val approvedDataSet = LineDataSet(approvedEntries, "").apply {
            color = ContextCompat.getColor(requireContext(), R.color.green_500)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.green_500))
            setDrawValues(false)
        }
        dataSets.add(approvedDataSet)

        val approvedAmounts = histories.map { it.status.approved }
        val min = approvedAmounts.minOrNull() ?: 0.0
        val max = approvedAmounts.maxOrNull() ?: 0.0
        val lineData = LineData(dataSets)

        binding.lineChart.apply {
            data = lineData
            description.isEnabled = false
            legend.isEnabled = false // Menonaktifkan legend
            setExtraOffsets(0f, 10f, 0f, 10f)

            xAxis.apply {
                granularity = 1f
                isGranularityEnabled = true
                valueFormatter = MonthAxisValueFormatter(histories.map { it.month })
                position = XAxis.XAxisPosition.BOTTOM
                yOffset = 10f
                axisMinimum = -0.5f
                axisMaximum = histories.size.toFloat() - 0.5f
                textColor = ContextCompat.getColor(requireContext(), R.color.text_color_setting)
            }

            axisRight.isEnabled = false

            axisLeft.apply {
                axisMinimum = min.toFloat()
                axisMaximum = max.toFloat()
                setDrawLabels(true)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return formatCurrency(value.toDouble())
                    }
                }
                setLabelCount(5, true)
                textColor = ContextCompat.getColor(requireContext(), R.color.text_color_setting) // Ganti warna label sumbu Y
            }

            animateY(500)
            invalidate()
        }
    }


    @SuppressLint("DefaultLocale")
    private fun formatCurrency(amount: Double): String {
        val formattedAmount = when {
            amount >= 1_000_000_000.0 -> {
                val billions = amount / 1_000_000_000.0
                String.format("%.1f M", billions)
            }
            amount >= 1_000_000.0 -> {
                val millions = amount / 1_000_000.0
                String.format("%.1f jt", millions)
            }
            else -> String.format("%,.0f", amount)
        }
        return "Rp $formattedAmount"
    }

    inner class MonthAxisValueFormatter(private val months: List<Int>) : ValueFormatter() {
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
