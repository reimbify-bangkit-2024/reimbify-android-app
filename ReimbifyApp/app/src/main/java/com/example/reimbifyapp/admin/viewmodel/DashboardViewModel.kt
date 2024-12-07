package com.example.reimbifyapp.admin.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.reimbifyapp.data.repositories.ReimbursementRepository
import com.example.reimbifyapp.data.network.response.AmountResponse
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
}