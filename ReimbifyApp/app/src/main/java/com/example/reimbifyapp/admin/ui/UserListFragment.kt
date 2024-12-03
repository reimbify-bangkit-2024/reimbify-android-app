package com.example.reimbifyapp.admin.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reimbifyapp.R
import com.example.reimbifyapp.admin.factory.UserListViewModelFactory
import com.example.reimbifyapp.admin.ui.adapter.UserListAdapter
import com.example.reimbifyapp.admin.ui.component.DeleteConfirmationDialog
import com.example.reimbifyapp.admin.ui.component.RegisterUserDialog
import com.example.reimbifyapp.admin.viewmodel.UserListViewModel
import com.example.reimbifyapp.auth.ui.component.SuccessDialogFragment
import com.example.reimbifyapp.data.entities.Department
import com.example.reimbifyapp.data.entities.User
import com.example.reimbifyapp.databinding.FragmentUserListAdminBinding
import com.example.reimbifyapp.utils.ErrorUtils.parseErrorMessage
import kotlin.getValue

class UserListFragment : Fragment() {

    private var _binding: FragmentUserListAdminBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<UserListViewModel> {
        UserListViewModelFactory.getInstance(requireContext())
    }

    private lateinit var adapter: UserListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserListAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchUserData()
        fetchDepartments()

        setupFab()
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun fetchUserData(departmentId: Int? = null, role: String? = null, search: String? = null, sort: Boolean? = null) {
        showLoading(false)
        viewModel.getAllUser(departmentId, role, search, sort == true)
    }

    private fun fetchDepartments() {
        viewModel.getAllDepartment()
    }

    private fun setupRecyclerView() {
        adapter = UserListAdapter(
            userList = ArrayList(),
            onDeleteClick = { user, position -> onDeleteUser(user, position) }
        )

        binding.rvRequest.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = this@UserListFragment.adapter
        }
    }
    private fun observeViewModel() {
        viewModel.allDepartment.observe(viewLifecycleOwner) { result ->
            result.getOrNull()?.let { setDepartmentSpinner(it.departments) }
        }

        viewModel.allUser.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                response.let {
                    if (it.users.isEmpty()) {
                        showNoUserMessage(true)
                    } else {
                        showNoUserMessage(false)
                        adapter.updateData(it.users)
                    }
                }
                showLoading(false)
            }.onFailure { throwable ->
                val errorMessage = parseErrorMessage(throwable)
                showToast(errorMessage)
                showNoUserMessage(true)
                showLoading(false)
            }
        }

        viewModel.userDeleted.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                fetchUserData()
            }.onFailure { throwable ->
                val errorMessage = parseErrorMessage(throwable)
                showToast(errorMessage)
            }
        }
    }

    private fun setDepartmentSpinner(departments: List<Department>) {
        val departmentNames = listOf("All") + departments.map { it.departmentName }
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, departmentNames)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerDepartment.adapter = spinnerAdapter
        binding.spinnerDepartment.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val departmentId = if (position == 0) null else departments[position - 1].departmentId
                fetchUserData(
                    departmentId = departmentId,
                    search = binding.etSearch.text.toString(),
                    sort = binding.btnSort.tag as? Boolean
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                fetchUserData(
                    departmentId = null,
                    search = binding.etSearch.text.toString(),
                    sort = binding.btnSort.tag as? Boolean
                )
            }
        }
    }

    private fun setupListeners() {
        binding.btnSearch.setOnClickListener {
            val searchQuery = binding.etSearch.text.toString()
            fetchUserData(search = searchQuery)
        }

        binding.btnSort.apply {
            tag = false
            setImageResource(R.drawable.sort_down_vertical_svgrepo_com)

            setOnClickListener {
                val isSortedDesc = tag as? Boolean == true
                tag = !isSortedDesc

                if (isSortedDesc) {
                    setImageResource(R.drawable.sort_down_vertical_svgrepo_com)
                } else {
                    setImageResource(R.drawable.sort_up_vertical_svgrepo_com)
                }

                fetchUserData(sort = !isSortedDesc)
            }
        }

        binding.spinnerRole.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val role = when (position) {
                    1 -> "admin"
                    2 -> "user"
                    else -> null
                }

                fetchUserData(
                    departmentId = binding.spinnerDepartment.selectedItemPosition.takeIf { it != 0 },
                    role = role.toString(),
                    search = binding.etSearch.text.toString(),
                    sort = binding.btnSort.tag as? Boolean
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                fetchUserData(
                    departmentId = binding.spinnerDepartment.selectedItemPosition.takeIf { it != 0 },
                    search = binding.etSearch.text.toString(),
                    sort = binding.btnSort.tag as? Boolean
                )
            }
        }
    }

    private fun showNoUserMessage(show: Boolean) {
        binding.rvRequest.visibility = if (show) View.GONE else View.VISIBLE
        binding.tvNoUsers.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.progressBar.isIndeterminate = isLoading
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun onDeleteUser(user: User, position: Int) {
        Log.d("USER DELETE", position.toString())

        val warningMessage = """
            <b>ID:</b> ${user.userId}<br>
            <b>Username:</b> ${user.userName}<br>
            <b>Department:</b> ${user.department.departmentName}<br>
            <b>Role:</b> ${user.role}
        """.trimIndent()

        DeleteConfirmationDialog.newInstance(
            warningMessage = warningMessage,
            onDeleteConfirmed = {
                viewModel.deleteUser(user.userId.toInt())
            },
            instance = "User"
        ).show(parentFragmentManager, DeleteConfirmationDialog.TAG)
    }

    private fun setupFab() {
        binding.fabAddUser.setOnClickListener {
            val registerUserDialog = RegisterUserDialog()
            registerUserDialog.show(parentFragmentManager, "RegisterUserDialog")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}