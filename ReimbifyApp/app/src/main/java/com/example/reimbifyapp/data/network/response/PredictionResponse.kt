package com.example.reimbifyapp.data.network.response

import com.example.reimbifyapp.data.entities.Crop
import com.example.reimbifyapp.data.entities.Rotate

data class PredictionResponse(
    val crop: Crop,
    val rotate: Rotate,
    val valid: Boolean
)