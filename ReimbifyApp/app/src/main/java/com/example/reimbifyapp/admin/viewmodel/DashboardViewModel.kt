package com.example.reimbifyapp.admin.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.reimbifyapp.data.repositories.ReimbursementRepository
import com.example.reimbifyapp.data.network.response.AmountResponse
import com.example.reimbifyapp.data.network.response.StatusResponse
import kotlinx.coroutines.Dispatchers

class DashboardViewModel(private val reimbursementRepository: ReimbursementRepository) : ViewModel() {

    fun getAmount(status: String) = liveData(Dispatchers.IO) {
        try {
            val response = reimbursementRepository.getAmount(status)
            Log.d("DashboardViewModel", "getAmount response: $response")
            emit(response)
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "Error fetching amount: ${e.message}")
            emit(AmountResponse(approvedAmount = 0.0, pendingAmount = 0.0))
        }
    }

    fun getMonthlyAmount(year: Int, status: String) = liveData(Dispatchers.IO) {
        try {
            val response = reimbursementRepository.getMonthlyAmount(year, status) // Mengirim status
            Log.d("DashboardViewModel", "getMonthlyAmount response: $response")
            emit(response)
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "Error fetching monthly amount: ${e.message}")
            emit(null)
        }
    }

    fun getTotalRequestStatus() = liveData(Dispatchers.IO) {
        try {
            val response = reimbursementRepository.getTotalRequestStatus()
            Log.d("DashboardViewModel", "getTotalRequestStatus response: $response")
            emit(response)
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "Error fetching total request status: ${e.message}")
            emit(StatusResponse(underReview = 0, approved = 0, rejected = 0)) // Emit nilai default jika error
        }
    }

    fun getTotalRequestByDepartment(status: String) = liveData(Dispatchers.IO) {
        try {
            val response = reimbursementRepository.getTotalRequestByDepartment(status)
            Log.d("DashboardViewModel", "getTotalRequestByDepartment response: $response")
            emit(response.histories) // Emit hanya bagian histories
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "Error fetching total request by department: ${e.message}")
            emit(emptyList())
        }
    }

}