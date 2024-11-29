package com.example.reimbifyapp.data.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserSession(
    val userId: String,
    val token: String,
    val role: String,
    val isLogin: Boolean = false
) : Parcelable