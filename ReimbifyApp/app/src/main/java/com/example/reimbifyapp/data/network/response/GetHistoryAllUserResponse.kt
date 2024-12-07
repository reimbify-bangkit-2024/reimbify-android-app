package com.example.reimbifyapp.data.network.response

data class GetHistoryAllUserResponse(
    val year: Int,
    val month: Int,
    val totalAmount: Long,
    val status: StatusResponse
)