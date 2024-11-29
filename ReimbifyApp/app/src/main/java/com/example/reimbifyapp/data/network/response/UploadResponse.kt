package com.example.reimbifyapp.data.network.response

data class UploadResponse(
    val success: Boolean,
    val message: String,
    val receiptImageUrl: String?
)
