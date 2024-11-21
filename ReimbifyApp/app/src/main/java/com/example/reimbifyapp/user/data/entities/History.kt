package com.example.reimbifyapp.user.data.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class History(
    val timestamp: String,
    val status: String,
    val receiptDate: String,
    val department: String,
    val amount: Double,
    val description: String
) : Parcelable