package com.example.reimbifyapp.user.data.network.api

import com.example.reimbifyapp.user.data.network.request.ForgotPasswordRequest
import com.example.reimbifyapp.user.data.network.request.LoginRequest
import com.example.reimbifyapp.user.data.network.request.VerifyOtpRequest
import com.example.reimbifyapp.user.data.network.response.ForgotPasswordResponse
import com.example.reimbifyapp.user.data.network.response.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @POST("auth/password/forgot")
    suspend fun forgotPassword(@Body forgotPasswordRequest: ForgotPasswordRequest): ForgotPasswordResponse

    @POST("/auth/otp/verify")
    suspend fun verifyOTP(@Body verifyOtpRequest: VerifyOtpRequest): LoginResponse
}