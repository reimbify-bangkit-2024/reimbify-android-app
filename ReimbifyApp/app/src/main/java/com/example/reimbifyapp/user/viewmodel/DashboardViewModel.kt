package com.example.reimbifyapp.user.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reimbifyapp.data.network.response.GetReimbursementResponse
import com.example.reimbifyapp.data.repositories.ReimbursementRepository
import com.example.reimbifyapp.data.repositories.UserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DashboardViewModel (
    private val userRepository: UserRepository,
    private val reimbursementRepository: ReimbursementRepository
) : ViewModel() {

    private val _underReviewResponse = MutableLiveData<Result<GetReimbursementResponse>>()
    val underReviewResponse: LiveData<Result<GetReimbursementResponse>> = _underReviewResponse

    fun getUnderReviewRequest() {
        viewModelScope.launch {
            try {
                val userId = userRepository.getSession().first().userId.toInt()
                val response = reimbursementRepository.getAllRequest(userId, false, null, null, "under review")
                _underReviewResponse.postValue(Result.success(response))
            } catch (e: Exception) {
                _underReviewResponse.postValue(Result.failure(e))
            }
        }
    }
}