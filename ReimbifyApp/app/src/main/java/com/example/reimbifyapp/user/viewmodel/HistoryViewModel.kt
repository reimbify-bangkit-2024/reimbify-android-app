package com.example.reimbifyapp.user.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reimbifyapp.data.network.response.GetDepartmentResponse
import com.example.reimbifyapp.data.network.response.GetReimbursementResponse
import com.example.reimbifyapp.data.repositories.DepartmentRepository
import com.example.reimbifyapp.data.repositories.ReimbursementRepository
import com.example.reimbifyapp.data.repositories.UserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val userRepository: UserRepository,
    private val reimbursementRepository: ReimbursementRepository,
    private val departmentRepository: DepartmentRepository
) : ViewModel() {

    private val _historyResponse = MutableLiveData<Result<GetReimbursementResponse>>()
    val historyResponse: LiveData<Result<GetReimbursementResponse>> = _historyResponse

    private val _departmentResponse = MutableLiveData<Result<GetDepartmentResponse>>()
    val departmentResponse: LiveData<Result<GetDepartmentResponse>> = _departmentResponse

    fun getRequest(search: String?, departmentId: Int?, sort: Boolean, status: String?) {
        viewModelScope.launch {
            val userId = userRepository.getSession().first().userId
            try {
                val response = reimbursementRepository.getAllRequest(userId.toInt(), sort, search, departmentId, status)
                _historyResponse.postValue(Result.success(response))
            } catch (e: Exception) {
                _historyResponse.postValue(Result.failure(e))
            }
        }
    }

    fun getAllDepartments() {
        viewModelScope.launch {
            try {
                val response = departmentRepository.getAllDepartment()
                _departmentResponse.postValue(Result.success(response))
            } catch (e: Exception) {
                _departmentResponse.postValue(Result.failure(e))
            }
        }
    }
}
