package com.example.reimbifyapp.data.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class History(
    val timestamp: String,
    val status: String,
    val receiptDate: String,
    val department: String,
    val amount: Double,
    val description: String,
    val adminName: String? = null,
    val accountNumber: String? = null,
    val receiveDate: String? = null,
    val declineDate: String? = null,
    val declineReason: String? = null,
    val notaImage: String,
    val transferReceiptImage: String? = null
) : Parcelable
