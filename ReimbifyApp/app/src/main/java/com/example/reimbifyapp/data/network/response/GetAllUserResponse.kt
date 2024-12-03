package com.example.reimbifyapp.data.network.response

import com.example.reimbifyapp.data.entities.User

data class GetAllUserResponse(
    val users: List<User>
)