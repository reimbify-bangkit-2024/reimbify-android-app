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
import com.example.reimbifyapp.admin.ui.adapter.RequestAdapter
import com.example.reimbifyapp.auth.factory.UserViewModelFactory
import com.example.reimbifyapp.auth.viewmodel.LoginViewModel
import com.example.reimbifyapp.data.entities.History
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
        setupReimbursementHistory(binding.lineChart)
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
                showToast("Failed to fetch session or user details: ${e.localizedMessage}")
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
                setUserName(user.user.userName)
            }
            result.onFailure { throwable ->
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
        val bundle = Bundle().apply {
            putParcelable("history_data", history)
        }
        findNavController().navigate(
            R.id.action_navigation_history_to_underReviewDetailFragment,
            bundle
        )
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
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
