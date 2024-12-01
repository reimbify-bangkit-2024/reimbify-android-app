package com.example.reimbifyapp.data.network.response

import com.example.reimbifyapp.data.entities.Bank

data class GetAllBankResponse(
    val banks: List<Bank>
)
