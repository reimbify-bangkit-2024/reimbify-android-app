package com.example.reimbifyapp.data.network.response

import com.example.reimbifyapp.data.entities.Blur
import com.example.reimbifyapp.data.entities.Crop
import com.example.reimbifyapp.data.entities.Rotate

data class PredictionResponse(
    val crop: Crop,
    val rotate: Rotate,
    val blur: Blur,
    val valid: Boolean
)