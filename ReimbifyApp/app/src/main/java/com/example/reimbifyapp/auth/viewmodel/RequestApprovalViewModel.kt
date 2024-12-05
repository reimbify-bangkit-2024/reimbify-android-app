package com.example.reimbifyapp.auth.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reimbifyapp.data.network.response.RequestApprovalResponse
import com.example.reimbifyapp.data.repositories.ReimbursementRepository
import kotlinx.coroutines.launch

class RequestApprovalViewModel (private val reimbursementRepository: ReimbursementRepository) : ViewModel() {
    private val _requestApproval = MutableLiveData<Result<RequestApprovalResponse>>()
    val requestApproval: LiveData<Result<RequestApprovalResponse>> = _requestApproval

    fun requestApproval(
        requestId: Int,
        status: String,
        adminId: Int,
        responseDescription: String
    ) {
        viewModelScope.launch {
            try {
                val response = reimbursementRepository.requestApproval(requestId, status, adminId, responseDescription)
                _requestApproval.postValue(Result.success(response))
            } catch (e: Exception) {
                _requestApproval.postValue(Result.failure(e))
            }
        }
    }
}