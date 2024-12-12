package com.example.reimbifyapp.admin.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reimbifyapp.data.network.response.GetDepartmentResponse
import com.example.reimbifyapp.data.network.response.GetReimbursementResponse
import com.example.reimbifyapp.data.repositories.DepartmentRepository
import com.example.reimbifyapp.data.repositories.ReimbursementRepository
import kotlinx.coroutines.launch

class ReviewedViewModel(
    private val reimbursementRepository: ReimbursementRepository,
    private val departmentRepository: DepartmentRepository
) : ViewModel() {

    private val _reviewedResponse = MutableLiveData<Result<GetReimbursementResponse>>()
    val reviewedResponse: LiveData<Result<GetReimbursementResponse>> = _reviewedResponse

    private val _departmentResponse = MutableLiveData<Result<GetDepartmentResponse>>()
    val departmentResponse: LiveData<Result<GetDepartmentResponse>> = _departmentResponse

    fun getRequest(search: String?, departmentId: Int?, sort: Boolean, status: String) {
        Log.d("STATUS", status)
        viewModelScope.launch {
            try {
                val response = reimbursementRepository.getAllRequest(null, sort, search, departmentId, status)
                _reviewedResponse.postValue(Result.success(response))
            } catch (e: Exception) {
                _reviewedResponse.postValue(Result.failure(e))
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
