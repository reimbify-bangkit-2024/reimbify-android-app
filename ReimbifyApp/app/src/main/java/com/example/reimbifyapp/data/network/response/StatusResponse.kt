package com.example.reimbifyapp.data.network.response

data class StatusResponse(
    val underReview: Long,
    val approved: Long,
    val rejected: Long
)
