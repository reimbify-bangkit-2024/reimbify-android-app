package com.example.reimbifyapp.data.entities

data class Account(
    val accountId: Int,
    val accountTitle: String,
    val accountHolderName: String,
    val accountNumber: String,
    val user: SimplifiedUser?,
    val bank: Bank
)