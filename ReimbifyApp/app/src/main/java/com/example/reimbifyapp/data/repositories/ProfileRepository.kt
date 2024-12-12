package com.example.reimbifyapp.data.repositories

import com.example.reimbifyapp.data.network.api.ApiService
import com.example.reimbifyapp.data.network.request.ChangePasswordRequest
import com.example.reimbifyapp.data.network.request.CreateBankAccountRequest
import com.example.reimbifyapp.data.network.request.GetBankAccountByUserIdRequest
import com.example.reimbifyapp.data.network.request.UpdateBankAccountRequest
import com.example.reimbifyapp.data.network.response.ChangePasswordResponse
import com.example.reimbifyapp.data.network.response.CreateBankAccountResponse
import com.example.reimbifyapp.data.network.response.DeleteBankAccountResponse
import com.example.reimbifyapp.data.network.response.GetAllBankResponse
import com.example.reimbifyapp.data.network.response.GetBankAccountByIdResponse
import com.example.reimbifyapp.data.network.response.GetBankAccountByUserIdResponse
import com.example.reimbifyapp.data.network.response.GetUserResponse
import com.example.reimbifyapp.data.network.response.UpdateBankAccountResponse

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

    suspend fun getAllBank() : GetAllBankResponse {
        return authApiService.getAllBank()
    }

    suspend fun createBankAccount(
        accountTitle: String,
        holderName: String,
        accountNumber: String,
        bankId: Int,
        userId: Int
    ) : CreateBankAccountResponse {
        val createBankAccountRequest = CreateBankAccountRequest(
            accountTitle, holderName, accountNumber, bankId, userId
        )
        return authApiService.createBankAccount(createBankAccountRequest)
    }

    suspend fun getBankAccountByUserId(
        userId: Int
    ): GetBankAccountByUserIdResponse {
        return authApiService.getBankAccountByUserId(userId)
    }

    suspend fun updateBankAccount(
        accountId: Int,
        accountTitle: String,
        accountHolderName: String,
        accountNumber: String,
        bankId: Int
    ): UpdateBankAccountResponse {
        val updateBankAccountRequest = UpdateBankAccountRequest(
            accountTitle, accountHolderName, accountNumber, bankId
        )
        return authApiService.updateBankAccount(accountId, updateBankAccountRequest)
    }

    suspend fun getBankAccountById(
        accountId: Int
    ): GetBankAccountByIdResponse {
        return authApiService.getBankAccountById(accountId)
    }

    suspend fun deleteBankAccount(
        accountId: Int
    ): DeleteBankAccountResponse {
        return authApiService.deleteBankAccount(accountId)
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