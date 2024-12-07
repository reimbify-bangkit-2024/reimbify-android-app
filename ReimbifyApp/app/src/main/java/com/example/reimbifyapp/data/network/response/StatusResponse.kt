package com.example.reimbifyapp.data.network.response

data class StatusResponse(
    val under_review: Long,
    val approved: Long,
    val rejected: Long
)
