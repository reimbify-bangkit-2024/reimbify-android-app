package com.example.reimbifyapp.user.data.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val userId: String,
    val token: String,
    val role: String,
    val isLogin: Boolean = false
) : Parcelable
