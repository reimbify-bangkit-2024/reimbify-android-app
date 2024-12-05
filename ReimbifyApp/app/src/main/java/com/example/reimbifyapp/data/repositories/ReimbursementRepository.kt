package com.example.reimbifyapp.data.repositories

import com.example.reimbifyapp.data.network.api.ApiService
import com.example.reimbifyapp.data.network.request.SendOtpRequest
import com.example.reimbifyapp.data.network.response.GetReimbursementResponse
import com.example.reimbifyapp.data.network.response.SendOtpResponse

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

        val paramSorted = if (sortedIncrement == true) "asc" else "desc"

        return authApiService.getAllRequest(
            userId = userId,
            sorted = paramSorted,
            search = search,
            departmentId = departmentId,
            status = paramStatus
        )
    }

    suspend fun getRequestById(receiptId: Int): GetReimbursementResponse {
        return authApiService.getRequestById(receiptId)
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