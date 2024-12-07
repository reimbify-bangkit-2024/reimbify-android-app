package com.example.reimbifyapp.data.network.response

import com.google.gson.annotations.SerializedName

data class AmountResponse(
    @SerializedName("approved") val approvedAmount: Double,
    @SerializedName("under_review") val pendingAmount: Double
)