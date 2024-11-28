package com.example.reimbifyapp.user.data.network.api

import com.example.reimbifyapp.user.data.network.request.ForgotPasswordRequest
import com.example.reimbifyapp.user.data.network.request.LoginRequest
import com.example.reimbifyapp.user.data.network.request.VerifyOtpRequest
import com.example.reimbifyapp.user.data.network.response.ForgotPasswordResponse
import com.example.reimbifyapp.user.data.network.response.LoginResponse
import com.example.reimbifyapp.user.data.network.response.UploadResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @POST("auth/password/forgot")
    suspend fun forgotPassword(@Body forgotPasswordRequest: ForgotPasswordRequest): ForgotPasswordResponse

    @POST("/auth/otp/verify")
    suspend fun verifyOTP(@Body verifyOtpRequest: VerifyOtpRequest): LoginResponse

    @Multipart
    @POST("request/image/upload")
    suspend fun uploadReceiptImage(
        @Header("Authorization") authToken: String,
        @Part image: MultipartBody.Part,
        @Query("userId") userId: String
    ): UploadResponse
}