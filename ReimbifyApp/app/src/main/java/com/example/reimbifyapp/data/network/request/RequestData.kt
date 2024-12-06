package com.example.reimbifyapp.data.network.request

data class RequestData(
    val requesterId: Int,
    val departmentId: Int,
    val accountId: Int,
    val receiptDate: String,
    val description: String,
    val amount: Double,
    val receiptImageUrl: String
)