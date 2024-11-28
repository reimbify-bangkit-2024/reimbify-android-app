package com.example.reimbifyapp.user.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reimbifyapp.user.data.network.response.ChangePasswordResponse
import com.example.reimbifyapp.user.data.network.response.GetUserResponse
import com.example.reimbifyapp.user.data.repositories.ProfileRepository
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {
    private val _getUserResult = MutableLiveData<Result<GetUserResponse>>()
    val getUserResult: LiveData<Result<GetUserResponse>> = _getUserResult

    private val _changePasswordResult = MutableLiveData<Result<ChangePasswordResponse>>()
    val changePasswordResult: LiveData<Result<ChangePasswordResponse>> = _changePasswordResult

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
        viewModelScope.launch {
            try {
                val response = repository.changePassword(userId, oldPassword, newPassword)
                _changePasswordResult.postValue(Result.success(response))
            } catch (e: Exception) {
                _changePasswordResult.postValue(Result.failure(e))
            }
        }
    }
}