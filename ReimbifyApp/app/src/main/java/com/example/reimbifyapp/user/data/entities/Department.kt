package com.example.reimbifyapp.user.data.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Department(
    val departmentId: Int,
    val departmentName: String
) : Parcelable