package com.example.reimbifyapp.data.network.response

import com.example.reimbifyapp.data.entities.Department

data class GetDepartmentResponse (
    val departments : List<Department>
)