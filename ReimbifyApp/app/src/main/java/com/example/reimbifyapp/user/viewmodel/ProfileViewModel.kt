package com.example.reimbifyapp.user.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reimbifyapp.data.network.api.ApiConfig
import com.example.reimbifyapp.data.network.response.ChangePasswordResponse
import com.example.reimbifyapp.data.network.response.CreateBankAccountResponse
import com.example.reimbifyapp.data.network.response.DeleteBankAccountResponse
import com.example.reimbifyapp.data.network.response.GetAllBankResponse
import com.example.reimbifyapp.data.network.response.GetBankAccountByIdResponse
import com.example.reimbifyapp.data.network.response.GetBankAccountByUserIdResponse
import com.example.reimbifyapp.data.network.response.GetUserResponse
import com.example.reimbifyapp.data.network.response.UpdateBankAccountResponse
import com.example.reimbifyapp.data.network.response.UploadResponse
import com.example.reimbifyapp.data.preferences.UserPreference
import com.example.reimbifyapp.data.repositories.ImageRepository
import com.example.reimbifyapp.data.repositories.ProfileRepository
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: ProfileRepository, private val imageRepository: ImageRepository, private val userPreference: UserPreference) : ViewModel() {
    private val _getUserResult = MutableLiveData<Result<GetUserResponse>>()
    val getUserResult: LiveData<Result<GetUserResponse>> = _getUserResult

    private val _changePasswordResult = MutableLiveData<Result<ChangePasswordResponse>>()
    val changePasswordResult: LiveData<Result<ChangePasswordResponse>> = _changePasswordResult

    private val _allBank = MutableLiveData<Result<GetAllBankResponse>>()
    val allBank: LiveData<Result<GetAllBankResponse>> = _allBank

    private val _createBankAccount = MutableLiveData<Result<CreateBankAccountResponse>>()
    val createBankAccount: LiveData<Result<CreateBankAccountResponse>> = _createBankAccount

    private val _bankAccountUserId = MutableLiveData<Result<GetBankAccountByUserIdResponse>>()
    val bankAccountUserId: LiveData<Result<GetBankAccountByUserIdResponse>> = _bankAccountUserId

    private val _updateBankAccount = MutableLiveData<Result<UpdateBankAccountResponse>>()
    val updateBankAccount: LiveData<Result<UpdateBankAccountResponse>> = _updateBankAccount

    private val _bankAccountById = MutableLiveData<Result<GetBankAccountByIdResponse>>()
    val bankAccountById: LiveData<Result<GetBankAccountByIdResponse>> = _bankAccountById

    private val _bankAccountDeleted = MutableLiveData<Result<DeleteBankAccountResponse>>()
    val bankAccountDeleted: LiveData<Result<DeleteBankAccountResponse>> = _bankAccountDeleted

    private val _uploadResponse = MutableLiveData<UploadResponse?>()
    val uploadResponse: LiveData<UploadResponse?> = _uploadResponse

    fun getUser(userId: String) {
        viewModelScope.launch {
            try {
                val response = repository.getUserInfo(userId)
                _getUserResult.postValue(Result.success(response))
            } catch (e: Exception) {
                _getUserResult.postValue(Result.failure(e))
            }
        }
    }

    fun changePassword(userId: String, oldPassword: String, newPassword: String) {
        Log.d("CHANGE PASS", "$userId $oldPassword $newPassword")
        viewModelScope.launch {
            try {
                val response = repository.changePassword(userId, oldPassword, newPassword)
                _changePasswordResult.postValue(Result.success(response))
            } catch (e: Exception) {
                _changePasswordResult.postValue(Result.failure(e))
            }
        }
    }

    fun getAllBank() {
        viewModelScope.launch {
            try {
                val response = repository.getAllBank()
                Log.d("ALL BANK", response.toString())
                _allBank.postValue(Result.success(response))
            } catch (e: Exception) {
                _allBank.postValue(Result.failure(e))
            }
        }
    }

    fun createBankAccount(
        accountTitle: String,
        holderName: String,
        accountNumber: String,
        bankId: Int,
        userId: Int
    ) {
        viewModelScope.launch {
            try {
                val response = repository.createBankAccount(accountTitle, holderName, accountNumber, bankId, userId)
                _createBankAccount.postValue(Result.success(response))
            } catch (e: Exception) {
                _createBankAccount.postValue(Result.failure(e))
            }
        }
    }

    fun getBankAccountByUserId(
        userId: Int
    ) {
        viewModelScope.launch {
            try {
                val response = repository.getBankAccountByUserId(userId)
                _bankAccountUserId.postValue(Result.success(response))
            } catch (e: Exception) {
                _bankAccountUserId.postValue(Result.failure(e))
            }
        }
    }

    fun updateBankAccount(
        accountId: Int,
        accountTitle: String,
        accountHolderName: String,
        accountNumber: String,
        bankId: Int
    ) {
        viewModelScope.launch {
            try {
                val response = repository.updateBankAccount(accountId, accountTitle, accountHolderName, accountNumber, bankId)
                _updateBankAccount.postValue(Result.success(response))
            } catch (e: Exception) {
                _updateBankAccount.postValue(Result.failure(e))
            }
        }
    }

    fun getBankAccountById(
        accountId: Int
    ) {
        viewModelScope.launch {
            try {
                val response = repository.getBankAccountById(accountId)
                _bankAccountById.postValue(Result.success(response))
            } catch (e: Exception) {
                _bankAccountById.postValue(Result.failure(e))
            }
        }
    }

    fun deleteBankAccount(
        accountId: Int
    ) {
        viewModelScope.launch {
            try {
                val response = repository.deleteBankAccount(accountId)
                _bankAccountDeleted.postValue(Result.success(response))
            } catch (e: Exception) {
                _bankAccountDeleted.postValue(Result.failure(e))
            }
        }
    }

    fun UploadImage(imageUri: Uri, userId: String){
        viewModelScope.launch {
            try {
                val apiService = ApiConfig.createAuthenticatedApiService(userPreference)
                val response = imageRepository.uploadImageProfile(apiService, imageUri, userId)
                _uploadResponse.value = response
            } catch (e: Exception) {
                Log.e("AddRequestViewModel", "Upload image failed: ${e.message}", e)
                _uploadResponse.value = null
            }
        }
    }
}