package com.example.reimbifyapp.data.entities

import java.util.Date

data class Reimbursement(
    val receiptId: Int,
    val requester: Requester,
    val department: Department,
    val account: Account,
    val receiptDate: Date,
    val description: String,
    val amount: Double,
    val requestDate: Date,
    val status: String,
    val receiptImageUrl: String,
    val approval: Approval?
)