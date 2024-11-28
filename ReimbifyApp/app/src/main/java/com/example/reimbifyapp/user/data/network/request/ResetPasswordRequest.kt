package com.example.reimbifyapp.user.data.network.request

data class ResetPasswordRequest(
    val userId: String,
    val otp: String,
    val newPassword: String
)
