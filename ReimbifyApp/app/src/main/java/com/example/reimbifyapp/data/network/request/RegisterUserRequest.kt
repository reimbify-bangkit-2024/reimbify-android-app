package com.example.reimbifyapp.data.network.request

data class RegisterUserRequest(
    val email: String,
    val password: String,
    val userName: String,
    val departmentId: Int,
    val role: String
)
