package com.example.reimbifyapp.user.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reimbifyapp.R
import com.example.reimbifyapp.admin.ui.adapter.RequestAdapter
import com.example.reimbifyapp.auth.factory.UserViewModelFactory
import com.example.reimbifyapp.auth.viewmodel.LoginViewModel
import com.example.reimbifyapp.data.entities.History
import com.example.reimbifyapp.data.network.response.GetHistoryAllUserResponse
import com.example.reimbifyapp.databinding.FragmentDashboardUserBinding
import com.example.reimbifyapp.user.factory.DashboardViewModelFactory
import com.example.reimbifyapp.user.factory.ProfileViewModelFactory
import com.example.reimbifyapp.user.viewmodel.DashboardViewModel
import com.example.reimbifyapp.user.viewmodel.ProfileViewModel
import com.example.reimbifyapp.utils.ErrorUtils.parseErrorMessage
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardUserBinding? = null
    private val binding get() = _binding!!

    private var isFetchingUser = false
    private var isFetchingRequests = false

    private val userViewModel by viewModels<LoginViewModel> {
        UserViewModelFactory.getInstance(requireContext())
    }

    private val profileViewModel by viewModels<ProfileViewModel> {
        ProfileViewModelFactory.getInstance(requireContext())
    }

    private val viewModel by viewModels<DashboardViewModel> {
        DashboardViewModelFactory.getInstance(requireContext())
    }

    private lateinit var adapter: RequestAdapter
    private var lastToastTime = 0L
    private val toastDelay = 5000L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchUserIdAndLoadUser()
        fetchRequests()

        setupRecyclerView()
        val year = Calendar.getInstance().get(Calendar.YEAR)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val session = userViewModel.getSession().first()
                val userId = session.userId.toInt()

                viewModel.getMonthlyAmountByUserId(year, userId).observe(viewLifecycleOwner) { listHistoryResponse ->
                    listHistoryResponse?.let { response ->
                        Log.d("DashboardFragment", "Fetched history data: ${response.histories}")
                        updateLineChart(response.histories)
                    }
                }

            } catch (e: Exception) {
                val errorMessage = parseErrorMessage(e)
                showToast("Failed to fetch monthly amount: $errorMessage")
            }
        }
        setupObserver()
    }

    private fun fetchUserIdAndLoadUser() {
        isFetchingUser = true
        updateLoadingState()
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
                updateLoadingState()
            }
        }
    }

    private fun fetchRequests() {
        isFetchingRequests = true
        updateLoadingState()
        viewModel.getUnderReviewRequest()
    }

    private fun setUserName(userName: String) {
        if (userName.isNotEmpty()) {
            binding.userGreeting.text = getString(R.string.greeting, userName)
        } else {
            _binding?.userGreeting?.text = getString(R.string.greeting, getString(R.string.default_user_name))
        }
    }

    private fun setupObserver() {
        profileViewModel.getUserResult.observe(viewLifecycleOwner) { result ->
            isFetchingUser = false
            updateLoadingState()

            result.onSuccess { user ->
                setUserName(user.users[0].userName)
            }
            result.onFailure { throwable ->
                println("Failed to load user profile: ${throwable.localizedMessage}")
                showToast("Failed to load user profile: ${throwable.localizedMessage}")
            }
        }

        viewModel.underReviewResponse.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                response.let {
                    if (it.receipts.isEmpty()) {
                        showNoRequestsMessage(true)
                    } else {
                        showNoRequestsMessage(false)
                        adapter.updateData(it.receipts)
                    }
                }
            }.onFailure { throwable ->
                val errorMessage = parseErrorMessage(throwable)
                showToast(errorMessage)
                showNoRequestsMessage(true)
            }.also {
                isFetchingRequests = false
                updateLoadingState()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = RequestAdapter(ArrayList())
        binding.underReviewRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = this@DashboardFragment.adapter
        }

        adapter.setOnItemClickCallback(object : RequestAdapter.OnItemClickCallback {
            override fun onItemClicked(data: History) {
                navigateToDetail(data)
            }
        })
    }

    private fun navigateToDetail(history: History) {
        val bundle = Bundle()
        bundle.putInt("requestId", history.id)

        findNavController().navigate(
            R.id.action_dashboard_to_requestDetailFragment,
            bundle
        )
    }

    private fun updateLineChart(histories: List<GetHistoryAllUserResponse>) {
        if (histories.isEmpty()) {
            binding.lineChart.clear()
            binding.lineChart.invalidate()
            return
        }

        val dataSets = mutableListOf<ILineDataSet>()
        val totalEntries = histories.mapIndexed { index, history ->
            val totalAmount = history.status.approved + history.status.under_review + history.status.rejected
            Entry(index.toFloat(), totalAmount.toFloat())
        }
        val totalDataSet = LineDataSet(totalEntries, "Total Amount").apply {
            color = android.graphics.Color.BLUE
            setCircleColor(android.graphics.Color.BLUE)
            setDrawValues(true)
            valueTextColor = android.graphics.Color.BLACK
            valueTextSize = 10f
        }
        dataSets.add(totalDataSet)
        val totalAmountSum = histories.sumOf { it.status.approved + it.status.under_review + it.status.rejected }
        val lineData = LineData(dataSets)
        binding.lineChart.apply {
            data = lineData
            description.isEnabled = false
            legend.isEnabled = false
            setExtraOffsets(20f, 0f, 0f, 20f)
            xAxis.apply {
                granularity = 1f
                isGranularityEnabled = true
                valueFormatter = MonthAxisValueFormatter(histories.map { it.month })
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                yOffset = 10f
            }
            axisRight.isEnabled = false
            axisLeft.apply {
                val min = histories.minOf { it.status.approved + it.status.under_review + it.status.rejected }
                val max = histories.maxOf { it.status.approved + it.status.under_review + it.status.rejected }
                axisMinimum = (min.toFloat() * 0.9f)
                axisMaximum = (max.toFloat() * 1.1f)
                setDrawLabels(true)
                setDrawGridLines(true)
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return formatCurrency(value.toDouble())
                    }
                }
                setLabelCount(5, true)
                setPosition(com.github.mikephil.charting.components.YAxis.YAxisLabelPosition.OUTSIDE_CHART)  // Move labels outside the chart
            }
            animateX(1000)
            invalidate()
        }

        Log.d("DashboardFragment", "Total Amount: $totalAmountSum")
        Log.d("DashboardFragment", "Total Amount Details: ${histories.joinToString { "Month: ${it.month}, Approved: ${it.status.approved}, Pending: ${it.status.under_review}, Rejected: ${it.status.rejected}" }}")
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

    private fun showNoRequestsMessage(show: Boolean) {
        binding.underReviewRecyclerView.visibility = if (show) View.GONE else View.VISIBLE
        binding.tvNoRequests.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun updateLoadingState() {
        showLoading(isFetchingUser || isFetchingRequests)
    }

    private fun showLoading(isLoading: Boolean) {
        _binding?.let { binding ->
            if (isLoading) {
                binding.loadingOverlay.visibility = View.VISIBLE
                binding.progressBar.isIndeterminate = true
            } else {
                binding.loadingOverlay.visibility = View.GONE
                binding.progressBar.isIndeterminate = false
            }
        }
    }

    private fun showToast(message: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastToastTime > toastDelay) {
            lastToastTime = currentTime
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}