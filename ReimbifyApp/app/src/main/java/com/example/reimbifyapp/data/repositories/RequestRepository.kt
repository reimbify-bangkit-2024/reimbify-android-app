package com.example.reimbifyapp.data.repositories

import com.example.reimbifyapp.data.network.api.ApiService
import com.example.reimbifyapp.data.network.request.RequestData
import com.example.reimbifyapp.data.network.response.SubmitRequestResponse
import retrofit2.Response

class RequestRepository private constructor(
    private val authApiService: ApiService,
    private val unAuthApiService: ApiService
) {

    suspend fun submitRequest(requestData: RequestData): Response<SubmitRequestResponse> {
        return try {
            val response = authApiService.uploadRequest(requestData)
            response
        } catch (e: Exception) {
            throw Exception("Failed to submit request: ${e.message}")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: RequestRepository? = null

        fun getInstance(
            authApiService: ApiService,
            unAuthApiService: ApiService
        ): RequestRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = RequestRepository(authApiService, unAuthApiService)
                INSTANCE = instance
                instance
            }
        }
    }
}
