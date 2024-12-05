package com.example.reimbifyapp.data.network.response

data class RequestApprovalResponse(
    val message: String,
    val receiptId: String,
    val status: String,
    val adminId: Int,
    val responseDate: String,
    val responseDescription: String
)
