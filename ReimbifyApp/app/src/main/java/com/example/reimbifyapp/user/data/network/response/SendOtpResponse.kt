package com.example.reimbifyapp.user.data.network.response

data class SendOtpResponse(
    val message: String,
    val userId: String,
    val email: String
)
