package com.example.reimbifyapp.data.repositories

import android.util.Log
import com.example.reimbifyapp.data.network.api.ApiService
import com.example.reimbifyapp.data.network.request.RequestApprovalRequest
import com.example.reimbifyapp.data.network.response.AmountResponse
import com.example.reimbifyapp.data.network.response.GetReimbursementResponse
import com.example.reimbifyapp.data.network.response.ListHistoryResponse
import com.example.reimbifyapp.data.network.response.ReimbursementCountResponse
import com.example.reimbifyapp.data.network.response.RequestApprovalResponse

class ReimbursementRepository private constructor(
    private val authApiService: ApiService,
    private val unAuthApiService: ApiService
) {
    suspend fun getAllRequest(
        userId: Int?,
        sortedIncrement: Boolean?,
        search: String?,
        departmentId: Int?,
        status: String?,
    ): GetReimbursementResponse {
        val paramStatus = when (status?.lowercase()) {
            "under review", "under-review", "under_review" -> "under_review"
            "rejected" -> "rejected"
            "approved" -> "approved"
            "rejected,approved", "approved,rejected" -> "rejected,approved"
            else -> null
        }

        val paramSorted = if (sortedIncrement == true) "request_date:asc" else "request_date:desc"

        return authApiService.getAllRequest(
            userId = userId,
            sorted = paramSorted,
            search = search,
            departmentId = departmentId,
            status = paramStatus
        )
    }

    suspend fun getAmount(status: String): AmountResponse {
        return authApiService.getAmounts(status)
    }

    suspend fun getRequestById(receiptId: Int): GetReimbursementResponse {
        return authApiService.getRequestById(receiptId)
    }

    suspend fun getMonthlyAmount(year: Int, status: String): ListHistoryResponse {
        return authApiService.getMonthlyAmount(year, status)
    }

    suspend fun requestApproval(
        receiptId: Int,
        status: String,
        adminId: Int,
        responseDescription: String
    ) : RequestApprovalResponse {
        val requestApprovalRequest = RequestApprovalRequest (
            status, adminId, responseDescription
        )

        Log.d("RECEIPT ID", receiptId.toString())
        Log.d("STATUS", status)
        return authApiService.requestApproval(receiptId, requestApprovalRequest)
    }

    companion object {
        @Volatile
        private var instance: ReimbursementRepository? = null
        fun getInstance(
            authApiService: ApiService,
            unAuthApiService: ApiService
        ): ReimbursementRepository =
            instance ?: synchronized(this) {
                instance ?: ReimbursementRepository(authApiService, unAuthApiService)
            }.also { instance = it }
    }
}