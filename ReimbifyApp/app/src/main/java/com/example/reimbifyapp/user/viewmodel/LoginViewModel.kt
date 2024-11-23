package com.example.reimbifyapp.user.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reimbifyapp.user.data.entities.User
import com.example.reimbifyapp.user.data.network.response.ForgotPasswordResponse
import com.example.reimbifyapp.user.data.network.response.LoginResponse
import com.example.reimbifyapp.user.data.repositories.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: UserRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<LoginResponse>>()
    val loginResult: LiveData<Result<LoginResponse>> = _loginResult


    private val _forgotPasswordResult = MutableLiveData<Result<ForgotPasswordResponse>>()
    val forgotPasswordResult: LiveData<Result<ForgotPasswordResponse>> = _forgotPasswordResult

    private val _verifyOtpResult = MutableLiveData<Result<LoginResponse>>()
    val verifyOtpResult: LiveData<Result<LoginResponse>> = _verifyOtpResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = repository.login(email, password)
                _loginResult.postValue(Result.success(response))
            } catch (e: Exception) {
                _loginResult.postValue(Result.failure(e))
            }
        }
    }

    fun forgotPassword(email: String, username: String) {
        viewModelScope.launch {
            try {
                val response = repository.forgotPassword(email, username)
                _forgotPasswordResult.postValue(Result.success(response))
            } catch (e: Exception) {
                _forgotPasswordResult.postValue(Result.failure(e))
            }
        }
    }

    fun verifyOtp(userId: String, otp: String) {
        viewModelScope.launch {
            try {
                val response = repository.verifyOtp(userId, otp)
                _verifyOtpResult.postValue(Result.success(response))
            } catch (e: Exception) {
                _verifyOtpResult.postValue(Result.failure(e))
            }
        }
    }

    fun saveSession(user: User) {
        viewModelScope.launch {
            repository.saveSession(user)
        }
    }

    fun getSession(): Flow<User> {
        return repository.getSession()
    }
}