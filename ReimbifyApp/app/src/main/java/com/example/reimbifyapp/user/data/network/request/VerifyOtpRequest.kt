package com.example.reimbifyapp.user.data.network.request

data class VerifyOtpRequest(
    val userId : String,
    val otp : String
)
