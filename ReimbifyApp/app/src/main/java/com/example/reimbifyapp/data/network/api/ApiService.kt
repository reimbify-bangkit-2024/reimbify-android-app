package com.example.reimbifyapp.data.network.api

import com.example.reimbifyapp.data.network.request.ChangePasswordRequest
import com.example.reimbifyapp.data.network.request.CreateBankAccountRequest
import com.example.reimbifyapp.data.network.request.ForgotPasswordRequest
import com.example.reimbifyapp.data.network.request.GetBankAccountByUserIdRequest
import com.example.reimbifyapp.data.network.request.LoginRequest
import com.example.reimbifyapp.data.network.request.ResetPasswordRequest
import com.example.reimbifyapp.data.network.request.SendOtpRequest
import com.example.reimbifyapp.data.network.request.UpdateBankAccountRequest
import com.example.reimbifyapp.data.network.request.VerifyOtpRequest
import com.example.reimbifyapp.data.network.response.ChangePasswordResponse
import com.example.reimbifyapp.data.network.response.CreateBankAccountResponse
import com.example.reimbifyapp.data.network.response.ForgotPasswordResponse
import com.example.reimbifyapp.data.network.response.GetAllBankResponse
import com.example.reimbifyapp.data.network.response.GetBankAccountByIdResponse
import com.example.reimbifyapp.data.network.response.GetBankAccountByUserIdResponse
import com.example.reimbifyapp.data.network.response.GetUserResponse
import com.example.reimbifyapp.data.network.response.LoginResponse
import com.example.reimbifyapp.data.network.response.ResetPasswordResponse
import com.example.reimbifyapp.data.network.response.SendOtpResponse
import com.example.reimbifyapp.data.network.response.UpdateBankAccountResponse
import com.example.reimbifyapp.data.network.response.UploadResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @POST("auth/password/forgot")
    suspend fun forgotPassword(@Body forgotPasswordRequest: ForgotPasswordRequest): ForgotPasswordResponse

    @POST("/auth/otp/verify")
    suspend fun verifyOTP(@Body verifyOtpRequest: VerifyOtpRequest): LoginResponse

    @POST("/auth/otp/resend")
    suspend fun sendOtp(@Body sendOtpRequest: SendOtpRequest): SendOtpResponse

    @POST("/auth/password/forgot")
    suspend fun resetPassword(@Body resetPasswordRequest: ResetPasswordRequest): ResetPasswordResponse

    @GET("/auth/users")
    suspend fun getUser(@Query("userId") userId: String): GetUserResponse

    @POST("/auth/password/update")
    suspend fun changePassword(@Body changePasswordRequest: ChangePasswordRequest): ChangePasswordResponse

    @GET("/bank")
    suspend fun getAllBank(): GetAllBankResponse

    @POST("/bank-account/create")
    suspend fun createBankAccount(@Body createBankAccountRequest: CreateBankAccountRequest): CreateBankAccountResponse

    @GET("/bank-account/accounts")
    suspend fun getBankAccountByUserId(@Query("userId") userId: Int): GetBankAccountByUserIdResponse

    @GET("/bank-account/accounts")
    suspend fun getBankAccountById(@Query("accountId") accountId: Int): GetBankAccountByIdResponse

    @PUT("/bank-account/update")
    suspend fun updateBankAccount(@Query("accountId") accountId: Int, @Body updateBankAccountRequest: UpdateBankAccountRequest): UpdateBankAccountResponse

    @Multipart
    @POST("request/image/upload")
    suspend fun uploadReceiptImage(
        @Header("Authorization") authToken: String,
        @Part image: MultipartBody.Part,
        @Query("userId") userId: String
    ): UploadResponse
}