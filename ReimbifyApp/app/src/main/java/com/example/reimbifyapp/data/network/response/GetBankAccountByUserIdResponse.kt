package com.example.reimbifyapp.data.network.response

import com.example.reimbifyapp.data.entities.Account

data class GetBankAccountByUserIdResponse(
    val accounts: List<Account>
)