package com.example.reimbifyapp.data.network.api

import com.example.reimbifyapp.data.network.request.ChangePasswordRequest
import com.example.reimbifyapp.data.network.request.CreateBankAccountRequest
import com.example.reimbifyapp.data.network.request.ForgotPasswordRequest
import com.example.reimbifyapp.data.network.request.LoginRequest
import com.example.reimbifyapp.data.network.request.RegisterUserRequest
import com.example.reimbifyapp.data.network.request.RequestApprovalRequest
import com.example.reimbifyapp.data.network.request.RequestData
import com.example.reimbifyapp.data.network.request.ResetPasswordRequest
import com.example.reimbifyapp.data.network.request.SendOtpRequest
import com.example.reimbifyapp.data.network.request.UpdateBankAccountRequest
import com.example.reimbifyapp.data.network.request.VerifyOtpRequest
import com.example.reimbifyapp.data.network.response.ChangePasswordResponse
import com.example.reimbifyapp.data.network.response.CreateBankAccountResponse
import com.example.reimbifyapp.data.network.response.DeleteUserResponse
import com.example.reimbifyapp.data.network.response.ForgotPasswordResponse
import com.example.reimbifyapp.data.network.response.GetAllBankResponse
import com.example.reimbifyapp.data.network.response.GetAllUserResponse
import com.example.reimbifyapp.data.network.response.GetBankAccountByIdResponse
import com.example.reimbifyapp.data.network.response.GetBankAccountByUserIdResponse
import com.example.reimbifyapp.data.network.response.GetDepartmentByIdResponse
import com.example.reimbifyapp.data.network.response.GetDepartmentResponse
import com.example.reimbifyapp.data.network.response.GetReimbursementResponse
import com.example.reimbifyapp.data.network.response.GetUserResponse
import com.example.reimbifyapp.data.network.response.LoginResponse
import com.example.reimbifyapp.data.network.response.RegisterUserResponse
import com.example.reimbifyapp.data.network.response.RequestApprovalResponse
import com.example.reimbifyapp.data.network.response.ResetPasswordResponse
import com.example.reimbifyapp.data.network.response.SendOtpResponse
import com.example.reimbifyapp.data.network.response.SubmitRequestResponse
import com.example.reimbifyapp.data.network.response.UpdateBankAccountResponse
import com.example.reimbifyapp.data.network.response.UploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
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

    @GET("/request")
    suspend fun getAllRequest(
        @Query("userId") userId: Int?,
        @Query("sorted") sorted: String?,
        @Query("search") search: String?,
        @Query("departmentId") departmentId: Int?,
        @Query("status") status: String?,
    ): GetReimbursementResponse

    @Multipart
    @POST("request/image/upload")
    suspend fun uploadReceiptImage(
        @Part image: MultipartBody.Part,
        @Query("userId") userId: String
    ): UploadResponse

    @GET("/department")
    suspend fun getAllDepartments() : GetDepartmentResponse

    @GET("/department")
    suspend fun getDepartmentById(@Query("departmentId") departmentId: Int) : GetDepartmentByIdResponse

    @GET("/auth/users")
    suspend fun getAllUsers(
        @Query("departmentId") departmentId: Int?,
        @Query("role") role: String?,
        @Query("search") search: String?,
        @Query("sorted") sorted: String?
    ) : GetAllUserResponse

    @DELETE("/auth/delete")
    suspend fun deleteUser(
        @Query("userId") userId: Int
    ) : DeleteUserResponse

    @POST("/auth/register")
    suspend fun registerUser(@Body registerUserRequest: RegisterUserRequest) : RegisterUserResponse
    @GET("/request")
    suspend fun getRequestById(@Query("receiptId") receiptId: Int) : GetReimbursementResponse

    @PATCH("/request/approval")
    suspend fun requestApproval(
        @Query("receiptId") receiptId: Int,
        @Body requestApprovalRequest: RequestApprovalRequest
    ) : RequestApprovalResponse

    @POST("request/create")
    suspend fun uploadRequest(
        @Body requestData: RequestData
    ): Response<SubmitRequestResponse>
}