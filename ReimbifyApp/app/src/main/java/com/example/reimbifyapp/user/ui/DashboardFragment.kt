package com.example.reimbifyapp.user.ui

import android.os.Bundle
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
import com.example.reimbifyapp.auth.factory.UserViewModelFactory
import com.example.reimbifyapp.auth.viewmodel.LoginViewModel
import com.example.reimbifyapp.data.entities.History
import com.example.reimbifyapp.databinding.FragmentDashboardUserBinding
import com.example.reimbifyapp.user.factory.ProfileViewModelFactory
import com.example.reimbifyapp.user.ui.adapter.HistoryAdapter
import com.example.reimbifyapp.user.viewmodel.ProfileViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardUserBinding? = null
    private val binding get() = _binding!!

    private val listUnderReview = ArrayList<History>()

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
        _binding = FragmentDashboardUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupReimbursementHistory(binding.lineChart)
        setupObserver()
        fetchUserIdAndLoadUser()
    }

    private fun fetchUserIdAndLoadUser() {
        showLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val session = userViewModel.getSession().first()
                val userId = session.userId

                profileViewModel.getUser(userId)
            } catch (e: Exception) {
                showToast("Failed to fetch session or user details: ${e.localizedMessage}")
            } finally {
                showLoading(false)
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

    private fun setupObserver() {
        profileViewModel.getUserResult.observe(viewLifecycleOwner) { result ->
            showLoading(false)
            result.onSuccess { user ->
                setUserName(user.user.userName)
            }
            result.onFailure { throwable ->
                showToast("Failed to load user profile: ${throwable.localizedMessage}")
            }
        }
    }

    private fun setupRecyclerView() {
        showLoading(true)
        if (listUnderReview.isEmpty()) {
            listUnderReview.addAll(getDummyHistory())
        }
        val adapter = HistoryAdapter(listUnderReview)

        binding.underReviewRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            this.adapter = adapter
        }

        showLoading(false)

        adapter.setOnItemClickCallback(object : HistoryAdapter.OnItemClickCallback {
            override fun onItemClicked(data: History) {
                navigateToUnderReviewDetail(data)
            }
        })
    }


    private fun setupReimbursementHistory(chart: LineChart) {
        val entries = listOf(
            Entry(0f, 200f),
            Entry(1f, 800f),
            Entry(2f, 600f),
            Entry(3f, 400f),
            Entry(4f, 600f)
        )

        val dataSet = LineDataSet(entries, "Reimbursement History").apply {
            lineWidth = 2f
            circleRadius = 4f
        }

        val lineData = LineData(dataSet)
        chart.apply {
            data = lineData
            description.isEnabled = false
            axisLeft.setDrawLabels(false)
            axisRight.setDrawLabels(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            invalidate()
        }
    }

    private fun getDummyHistory(): ArrayList<History> {
        return arrayListOf(
            History(
                id = 1,
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
                id = 2,
                timestamp = "2024-11-20",
                status = "Under Review",
                receiptDate = "2024-11-19",
                department = "HR",
                amount = 50000.0,
                description = "Travel allowance reimbursement",
                adminName = null,
                accountNumber = null,
                receiveDate = null,
                declineDate = null,
                declineReason = null,
                notaImage = "https://example.com/nota_under_review.jpg",
                transferReceiptImage = null
            ),
            History(
                id = 3,
                timestamp = "2024-11-19",
                status = "Under Review",
                receiptDate = "2024-11-18",
                department = "IT",
                amount = 30000.0,
                description = "Equipment purchase reimbursement",
                adminName = null,
                accountNumber = null,
                receiveDate = null,
                declineDate = null,
                declineReason = null,
                notaImage = "https://example.com/nota_under_review.jpg",
                transferReceiptImage = null
            )
        )
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
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
