package com.example.reimbifyapp.data.network.response

import com.example.reimbifyapp.data.entities.User

data class DeleteUserResponse (
    val message: String,
    val user: User
)