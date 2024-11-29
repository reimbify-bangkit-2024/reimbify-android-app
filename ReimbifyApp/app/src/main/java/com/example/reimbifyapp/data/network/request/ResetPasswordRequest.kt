package com.example.reimbifyapp.data.network.request

data class ResetPasswordRequest(
    val userId: String,
    val otp: String,
    val newPassword: String
)
