package com.example.reimbifyapp.user.data.network.response


data class ResetPasswordResponse(
    val message: String,
    val userId: String,
    val email: String,
    val role: String
)