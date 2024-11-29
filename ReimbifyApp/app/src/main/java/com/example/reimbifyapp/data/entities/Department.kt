package com.example.reimbifyapp.data.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Department(
    val departmentId: Int,
    val departmentName: String
) : Parcelable