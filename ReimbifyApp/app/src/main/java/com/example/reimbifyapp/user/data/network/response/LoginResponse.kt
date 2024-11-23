package com.example.reimbifyapp.user.data.network.response

data class LoginResponse(
    val message: String,
    val userId: String,
    val accessToken: String,
    val role: String,
)