package com.example.reimbifyapp.data.network.request

data class GetReimbursementRequest(
    val userId: String?,
    val sorted: String?,
    val search: String?,
    val departmentId: String?,
    val status: String?,
)
