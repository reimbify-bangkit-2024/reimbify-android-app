package com.example.reimbifyapp.data.network.response

import com.example.reimbifyapp.data.entities.Reimbursement

data class GetReimbursementResponse(
    val receipts: List<Reimbursement>
)
