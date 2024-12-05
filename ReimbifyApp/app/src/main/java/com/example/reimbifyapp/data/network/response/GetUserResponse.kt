package com.example.reimbifyapp.data.network.response

import com.example.reimbifyapp.data.entities.User

data class GetUserResponse(
    val users: List<User>
)