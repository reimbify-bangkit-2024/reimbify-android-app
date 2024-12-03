package com.example.reimbifyapp.admin.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reimbifyapp.data.network.response.DeleteUserResponse
import com.example.reimbifyapp.data.network.response.GetAllUserResponse
import com.example.reimbifyapp.data.network.response.GetDepartmentResponse
import com.example.reimbifyapp.data.network.response.RegisterUserResponse
import com.example.reimbifyapp.data.repositories.DepartmentRepository
import com.example.reimbifyapp.data.repositories.UserRepository
import kotlinx.coroutines.launch

class UserListViewModel (
    private val userRepository: UserRepository,
    private val departmentRepository: DepartmentRepository
) : ViewModel() {

    private val _allUser = MutableLiveData<Result<GetAllUserResponse>>()
    val allUser: LiveData<Result<GetAllUserResponse>> = _allUser

    private val _allDepartment = MutableLiveData<Result<GetDepartmentResponse>>()
    val allDepartment: LiveData<Result<GetDepartmentResponse>> = _allDepartment

    private val _userDeleted = MutableLiveData<Result<DeleteUserResponse>>()
    val userDeleted: LiveData<Result<DeleteUserResponse>> = _userDeleted

    private val _registerUser = MutableLiveData<Result<RegisterUserResponse>>()
    val registerUser: LiveData<Result<RegisterUserResponse>> = _registerUser

    fun getAllUser(departmentId: Int?, role: String?, search: String?, sort: Boolean) {
        viewModelScope.launch {
            try {
                val response = userRepository.getAllUser(departmentId, role, search, sort)
                _allUser.postValue(Result.success(response))
            } catch (e: Exception) {
                _allUser.postValue(Result.failure(e))
            }
        }
    }

    fun getAllDepartment() {
        viewModelScope.launch {
            try {
                val response = departmentRepository.getAllDepartment()
                _allDepartment.postValue(Result.success(response))
            } catch (e: Exception) {
                _allDepartment.postValue(Result.failure(e))
            }
        }
    }

    fun deleteUser(userId: Int) {
        viewModelScope.launch {
            try {
                val response = userRepository.deleteUser(userId)
                _userDeleted.postValue(Result.success(response))
            } catch (e: Exception) {
                _userDeleted.postValue(Result.failure(e))
            }
        }
    }

    fun registerUser(
        email: String,
        password: String,
        userName: String,
        departmentId: Int,
        role: String
    ) {
        viewModelScope.launch {
            try {
                val response = userRepository.registerUser(email, password, userName, departmentId, role)
                _registerUser.postValue(Result.success(response))
            } catch (e: Exception) {
                _registerUser.postValue(Result.failure(e))
            }
        }
    }
}