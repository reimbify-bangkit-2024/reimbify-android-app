package com.example.reimbifyapp.data.network.response

data class SendOtpResponse(
    val message: String,
    val userId: String,
    val email: String
)
