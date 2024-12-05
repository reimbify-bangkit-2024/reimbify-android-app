package com.example.reimbifyapp.user.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reimbifyapp.R
import com.example.reimbifyapp.user.factory.HistoryViewModelFactory
import com.example.reimbifyapp.user.viewmodel.HistoryViewModel
import com.example.reimbifyapp.data.entities.Department
import com.example.reimbifyapp.data.entities.History
import com.example.reimbifyapp.databinding.FragmentHistoryUserBinding
import com.example.reimbifyapp.user.ui.adapter.HistoryAdapter
import com.example.reimbifyapp.utils.ErrorUtils.parseErrorMessage

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryUserBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<HistoryViewModel> {
        HistoryViewModelFactory.getInstance(requireContext())
    }

    private lateinit var adapter: HistoryAdapter
    private var lastToastTime = 0L
    private val toastDelay = 5000L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchDepartments()
        fetchRequests(sort = false)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter(ArrayList())
        binding.rvRequest.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = this@HistoryFragment.adapter
        }

        adapter.setOnItemClickCallback(object : HistoryAdapter.OnItemClickCallback {
            override fun onItemClicked(data: History) {
                navigateToDetail(data)
            }
        })
    }

    private fun observeViewModel() {
        viewModel.departmentResponse.observe(viewLifecycleOwner) { result ->
            result.getOrNull()?.let { setDepartmentSpinner(it.departments) }
        }

        viewModel.historyResponse.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                response.let {
                    if (it.receipts.isEmpty()) {
                        showNoRequestsMessage(true)
                    } else {
                        showNoRequestsMessage(false)
                        adapter.updateData(it.receipts)
                    }
                }
                showLoading(false)
            }.onFailure { throwable ->
                val errorMessage = parseErrorMessage(throwable)
                showToast(errorMessage)
                showNoRequestsMessage(true)
                showLoading(false)
            }
        }
    }

    private fun fetchDepartments() {
        viewModel.getAllDepartments()
    }

    private fun fetchRequests(search: String? = null, departmentId: Int? = null, sort: Boolean? = null, status: String? = null) {
        showLoading(true)
        viewModel.getRequest(search, departmentId, sort == true, status)
    }

    private fun setDepartmentSpinner(departments: List<Department>) {
        val departmentNames = listOf("All") + departments.map { it.departmentName }
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, departmentNames)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerDepartment.adapter = spinnerAdapter
        binding.spinnerDepartment.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val departmentId = if (position == 0) null else departments[position - 1].departmentId
                fetchRequests(
                    search = binding.etSearch.text.toString(),
                    departmentId = departmentId,
                    sort = binding.btnSort.tag as? Boolean
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupListeners() {
        binding.btnSearch.setOnClickListener {
            val searchQuery = binding.etSearch.text.toString()
            fetchRequests(search = searchQuery)
        }

        binding.btnSort.apply {
            tag = false
            setImageResource(R.drawable.sort_down_vertical_svgrepo_com)

            setOnClickListener {
                val isSortedDesc = tag as? Boolean == true
                tag = !isSortedDesc

                Log.d("SORT is", isSortedDesc.toString())
                Log.d("SORT tag", tag.toString())

                if (isSortedDesc) {
                    setImageResource(R.drawable.sort_down_vertical_svgrepo_com)
                } else {
                    setImageResource(R.drawable.sort_up_vertical_svgrepo_com)
                }

                fetchRequests(sort = !isSortedDesc)
            }
        }

        binding.spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val status = when (position) {
                    1 -> "under review"
                    2 -> "approved"
                    3 -> "rejected"
                    else -> null
                }

                fetchRequests(
                    search = binding.etSearch.text.toString(),
                    departmentId = binding.spinnerDepartment.selectedItemPosition.takeIf { it != 0 },
                    sort = binding.btnSort.tag as? Boolean,
                    status = status.toString()
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                fetchRequests(
                    search = binding.etSearch.text.toString(),
                    departmentId = binding.spinnerDepartment.selectedItemPosition.takeIf { it != 0 },
                    sort = binding.btnSort.tag as? Boolean
                )
            }
        }
    }

    private fun navigateToDetail(history: History) {
        showToast("Navigate to Detail Page: ${history.id} ${history.status}")
//        val bundle = Bundle().apply {
//            putParcelable("history_data", history)
//        }
//        findNavController().navigate(R.id.action_navigation_history_to_underReviewDetailFragment, bundle)
    }

    private fun showNoRequestsMessage(show: Boolean) {
        binding.rvRequest.visibility = if (show) View.GONE else View.VISIBLE
        binding.tvNoRequests.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.progressBar.isIndeterminate = isLoading
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
