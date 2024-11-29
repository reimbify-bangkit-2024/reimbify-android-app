package com.example.reimbifyapp.data.network.request

data class ChangePasswordRequest(
    val userId: String,
    val oldPassword: String,
    val newPassword: String
)