package com.example.reimbifyapp.data.network.request

data class CreateBankAccountRequest(
    val accountTitle: String,
    val accountHolderName: String,
    val accountNumber: String,
    val bankId: Int,
    val userId: Int
)
