package com.example.reimbifyapp.user.data.repositories

import com.example.reimbifyapp.user.data.entities.User
import com.example.reimbifyapp.user.data.network.api.ApiConfig
import com.example.reimbifyapp.user.data.network.request.ForgotPasswordRequest
import com.example.reimbifyapp.user.data.network.request.LoginRequest
import com.example.reimbifyapp.user.data.network.request.VerifyOtpRequest
import com.example.reimbifyapp.user.data.network.response.ForgotPasswordResponse
import com.example.reimbifyapp.user.data.network.response.LoginResponse
import com.example.reimbifyapp.user.data.preferences.UserPreference
import kotlinx.coroutines.flow.Flow

class UserRepository private constructor(
    private val userPreference: UserPreference
) {

    suspend fun saveSession(user: User) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<User> {
        return userPreference.getSession()
    }

    suspend fun login(email: String, password: String): LoginResponse {
        val loginRequest = LoginRequest(email, password)
        return ApiConfig.apiService.login(loginRequest)
    }

    suspend fun logout() {
        userPreference.logout()
    }

    suspend fun forgotPassword(email: String, username: String) : ForgotPasswordResponse {
        val forgotPasswordRequest = ForgotPasswordRequest(email)
        return ApiConfig.apiService.forgotPassword(forgotPasswordRequest)
    }

    suspend fun verifyOtp(userId: String, otp: String) : LoginResponse {
        val verifyOtpRequest = VerifyOtpRequest(userId, otp)
        return ApiConfig.apiService.verifyOTP(verifyOtpRequest)
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            userPreference: UserPreference
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference)
            }.also { instance = it }
    }
}