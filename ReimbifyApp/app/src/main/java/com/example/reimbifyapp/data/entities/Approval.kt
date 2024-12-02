package com.example.reimbifyapp.data.entities

import java.util.Date

data class Approval(
    val admin: User,
    val responseDate: Date,
    val transferImageUrl: String,
    val responseDescription: String
)
