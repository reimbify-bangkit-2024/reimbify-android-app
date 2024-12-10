package com.example.reimbifyapp.data.repositories

import android.icu.text.StringSearch
import com.example.reimbifyapp.data.entities.UserSession
import com.example.reimbifyapp.data.network.api.ApiConfig
import com.example.reimbifyapp.data.network.api.ApiService
import com.example.reimbifyapp.data.network.request.ForgotPasswordRequest
import com.example.reimbifyapp.data.network.request.LoginRequest
import com.example.reimbifyapp.data.network.request.RegisterUserRequest
import com.example.reimbifyapp.data.network.request.ResetPasswordRequest
import com.example.reimbifyapp.data.network.request.SendOtpRequest
import com.example.reimbifyapp.data.network.request.VerifyOtpRequest
import com.example.reimbifyapp.data.network.response.DeleteUserResponse
import com.example.reimbifyapp.data.network.response.ForgotPasswordResponse
import com.example.reimbifyapp.data.network.response.GetAllUserResponse
import com.example.reimbifyapp.data.network.response.LoginResponse
import com.example.reimbifyapp.data.network.response.RegisterUserResponse
import com.example.reimbifyapp.data.network.response.ResetPasswordResponse
import com.example.reimbifyapp.data.network.response.SendOtpResponse
import com.example.reimbifyapp.data.preferences.UserPreference
import kotlinx.coroutines.flow.Flow

class UserRepository private constructor(
    private val userPreference: UserPreference,
    private val authApiService: ApiService,
    private val unAuthApiService: ApiService
) {

    suspend fun saveSession(userSession: UserSession) {
        userPreference.saveSession(userSession)
    }

    fun getSession(): Flow<UserSession> {
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
        return unAuthApiService.forgotPassword(forgotPasswordRequest)
    }

    suspend fun verifyOtp(userId: String, otp: String) : LoginResponse {
        val verifyOtpRequest = VerifyOtpRequest(userId, otp)
        return unAuthApiService.verifyOTP(verifyOtpRequest)
    }

    suspend fun sendOtp(userId: String) : SendOtpResponse {
        val sendOtpRequest = SendOtpRequest(userId)
        return unAuthApiService.sendOtp(sendOtpRequest)
    }

    suspend fun resetPassword(userId: String, otp: String, newPassword: String) : ResetPasswordResponse {
        val resetPasswordRequest = ResetPasswordRequest(userId, otp, newPassword)
        return authApiService.resetPassword(resetPasswordRequest)
    }

    suspend fun getAllUser(departmentId: Int?, role: String?, search: String?, sortedIncrement: Boolean) : GetAllUserResponse {
        val paramSorted = if (sortedIncrement == true) "user_name:asc" else "user_name:desc"

        return authApiService.getAllUsers(departmentId, role?.lowercase(), search, paramSorted)
    }

    suspend fun deleteUser(userId: Int) : DeleteUserResponse {
        return authApiService.deleteUser(userId)
    }

    suspend fun registerUser(email: String, password: String, userName: String, departmentId: Int, role: String) : RegisterUserResponse {
        val registerUserRequest = RegisterUserRequest(
            email,
            password,
            userName,
            departmentId,
            role.lowercase()
        )

        return authApiService.registerUser(registerUserRequest)
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            userPreference: UserPreference,
            authApiService: ApiService,
            unAuthApiService: ApiService
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference, authApiService, unAuthApiService)
            }.also { instance = it }
    }
}