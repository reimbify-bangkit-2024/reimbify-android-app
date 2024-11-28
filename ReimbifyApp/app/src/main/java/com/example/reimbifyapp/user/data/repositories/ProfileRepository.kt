package com.example.reimbifyapp.user.data.repositories

import com.example.reimbifyapp.user.data.network.api.ApiService
import com.example.reimbifyapp.user.data.network.request.ChangePasswordRequest
import com.example.reimbifyapp.user.data.network.response.ChangePasswordResponse
import com.example.reimbifyapp.user.data.network.response.GetUserResponse

class ProfileRepository private constructor(
    private val authApiService: ApiService,
    private val unAuthApiService: ApiService
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
            authApiService: ApiService,
            unAuthApiService: ApiService
        ): ProfileRepository =
            instance ?: synchronized(this) {
                instance ?: ProfileRepository(authApiService, unAuthApiService)
            }.also { instance = it }
    }
}