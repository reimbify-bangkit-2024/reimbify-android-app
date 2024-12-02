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

class ToReviewViewModel(
    private val reimbursementRepository: ReimbursementRepository,
    private val departmentRepository: DepartmentRepository
) : ViewModel() {

    private val _underReviewResponse = MutableLiveData<Result<GetReimbursementResponse>>()
    val underReviewResponse: LiveData<Result<GetReimbursementResponse>> = _underReviewResponse

    private val _departmentResponse = MutableLiveData<Result<GetDepartmentResponse>>()
    val departmentResponse: LiveData<Result<GetDepartmentResponse>> = _departmentResponse

    fun getUnderReviewRequest(search: String?, departmentId: Int?, sort: Boolean) {
        Log.d("SORT", sort.toString())
        viewModelScope.launch {
            try {
                val response = reimbursementRepository.getAllRequest(null, sort, search, departmentId, "under review")
                _underReviewResponse.postValue(Result.success(response))
            } catch (e: Exception) {
                _underReviewResponse.postValue(Result.failure(e))
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
