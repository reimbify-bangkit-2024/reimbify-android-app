package com.example.reimbifyapp.data.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val userId: String,
    val email: String,
    val userName: String,
    val department: Department,
    val role: String,
    val profileImageUrl: String?
) : Parcelable