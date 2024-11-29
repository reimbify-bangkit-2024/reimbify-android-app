package com.example.reimbifyapp.data.repositories

import com.example.reimbifyapp.data.network.api.ApiService
import com.example.reimbifyapp.data.network.request.ChangePasswordRequest
import com.example.reimbifyapp.data.network.response.ChangePasswordResponse
import com.example.reimbifyapp.data.network.response.GetUserResponse

class ProfileRepository private constructor(
    private val authApiService: com.example.reimbifyapp.data.network.api.ApiService,
    private val unAuthApiService: com.example.reimbifyapp.data.network.api.ApiService
) {
    suspend fun getUserInfo(userId: String): GetUserResponse {
        return authApiService.getUser(userId)
    }

    suspend fun changePassword(userId: String, oldPassword: String, newPassword: String): ChangePasswordResponse {
        val changePasswordRequest = ChangePasswordRequest(
            userId, oldPassword, newPassword
        )
        return authApiService.changePassword(changePasswordRequest)
    }

    companion object {
        @Volatile
        private var instance: ProfileRepository? = null
        fun getInstance(
            authApiService: com.example.reimbifyapp.data.network.api.ApiService,
            unAuthApiService: com.example.reimbifyapp.data.network.api.ApiService
        ): ProfileRepository =
            instance ?: synchronized(this) {
                instance ?: ProfileRepository(authApiService, unAuthApiService)
            }.also { instance = it }
    }
}