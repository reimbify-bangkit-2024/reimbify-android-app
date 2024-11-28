package com.example.reimbifyapp.user.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reimbifyapp.user.data.entities.UserSession
import com.example.reimbifyapp.user.data.network.response.ForgotPasswordResponse
import com.example.reimbifyapp.user.data.network.response.LoginResponse
import com.example.reimbifyapp.user.data.network.response.ResetPasswordResponse
import com.example.reimbifyapp.user.data.network.response.SendOtpResponse
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


    private val _sendOtpResult = MutableLiveData<Result<SendOtpResponse>>()
    val sendOtpResult: LiveData<Result<SendOtpResponse>> = _sendOtpResult

    private val _resetPasswordResult = MutableLiveData<Result<ResetPasswordResponse>>()
    val resetPasswordResult: LiveData<Result<ResetPasswordResponse>> = _resetPasswordResult

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

    suspend fun logout() {
        repository.logout()
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

    fun sendOtp(userId: String) {
        viewModelScope.launch {
            try {
                val response = repository.sendOtp(userId)
                _sendOtpResult.postValue(Result.success(response))
            } catch (e: Exception) {
                _sendOtpResult.postValue(Result.failure(e))
            }
        }
    }

    fun resetPassword(userId: String, otp: String, newPassword: String) {
        viewModelScope.launch {
            try {
                val response = repository.resetPassword(userId, otp, newPassword)
                _resetPasswordResult.postValue(Result.success(response))
            } catch (e: Exception) {
                _resetPasswordResult.postValue(Result.failure(e))
            }
        }
    }

    fun saveSession(userSession: UserSession) {
        viewModelScope.launch {
            repository.saveSession(userSession)
        }
    }

    fun getSession(): Flow<UserSession> {
        return repository.getSession()
    }
}