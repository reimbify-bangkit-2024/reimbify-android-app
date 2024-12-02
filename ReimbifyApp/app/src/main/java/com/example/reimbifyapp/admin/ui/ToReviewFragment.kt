package com.example.reimbifyapp.admin.ui

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
import com.example.reimbifyapp.admin.factory.ToReviewViewModelFactory
import com.example.reimbifyapp.admin.viewmodel.ToReviewViewModel
import com.example.reimbifyapp.data.entities.Department
import com.example.reimbifyapp.data.entities.History
import com.example.reimbifyapp.databinding.FragmentToReviewAdminBinding
import com.example.reimbifyapp.admin.ui.adapter.RequestAdapter
import com.example.reimbifyapp.utils.ErrorUtils.parseErrorMessage

class ToReviewFragment : Fragment() {

    private var _binding: FragmentToReviewAdminBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<ToReviewViewModel> {
        ToReviewViewModelFactory.getInstance(requireContext())
    }

    private lateinit var adapter: RequestAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentToReviewAdminBinding.inflate(inflater, container, false)
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
        adapter = RequestAdapter(ArrayList())
        binding.rvRequest.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = this@ToReviewFragment.adapter
        }

        adapter.setOnItemClickCallback(object : RequestAdapter.OnItemClickCallback {
            override fun onItemClicked(data: History) {
                navigateToDetail(data)
            }
        })
    }

    private fun observeViewModel() {
        viewModel.departmentResponse.observe(viewLifecycleOwner) { result ->
            result.getOrNull()?.let { setDepartmentSpinner(it.departments) }
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

    private fun fetchRequests(search: String? = null, departmentId: Int? = null, sort: Boolean? = null) {
        showLoading(true)
        viewModel.getUnderReviewRequest(search, departmentId, sort == true)
    }

    private fun setDepartmentSpinner(departments: List<Department>) {
        val departmentNames = listOf("All Departments") + departments.map { it.departmentName }
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
    }

    private fun navigateToDetail(history: History) {
        showToast("Navigate to Approval Page: ${history.id}")
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
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
