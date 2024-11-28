package com.example.reimbifyapp.user.data.network.request

data class ChangePasswordRequest(
    val userId: String,
    val oldPassword: String,
    val newPassword: String
)