package com.example.reimbifyapp.data.network.request

data class RequestApprovalRequest (
    val status: String,
    val adminId: Int,
    val responseDescription: String
)