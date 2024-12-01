package com.example.reimbifyapp.data.network.request

data class UpdateBankAccountRequest(
    val accountTitle: String,
    val accountHolderName: String,
    val accountNumber: String,
    val bankId: Int
)
