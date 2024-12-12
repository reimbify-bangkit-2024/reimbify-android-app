package com.example.reimbifyapp.auth.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reimbifyapp.data.network.response.GetReimbursementResponse
import com.example.reimbifyapp.data.repositories.ReimbursementRepository
import kotlinx.coroutines.launch

class RequestDetailViewModel(private val repository: ReimbursementRepository) : ViewModel() {
    private val _requestDetail = MutableLiveData<Result<GetReimbursementResponse>>()
    val requestDetail: LiveData<Result<GetReimbursementResponse>> = _requestDetail

    fun getRequestDetail(requestId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getRequestById(requestId)
                _requestDetail.postValue(Result.success(response))
            } catch (e: Exception) {
                _requestDetail.postValue(Result.failure(e))
            }
        }
    }
}