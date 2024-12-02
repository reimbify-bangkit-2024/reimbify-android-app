package com.example.reimbifyapp.data.repositories

import com.example.reimbifyapp.data.network.api.ApiService
import com.example.reimbifyapp.data.network.response.GetDepartmentByIdResponse
import com.example.reimbifyapp.data.network.response.GetDepartmentResponse
import com.example.reimbifyapp.data.network.response.GetReimbursementResponse

class DepartmentRepository private constructor(
    private val authApiService: ApiService,
    private val unAuthApiService: ApiService
) {
    suspend fun getAllDepartment(): GetDepartmentResponse {
        return authApiService.getAllDepartments()
    }

    suspend fun getDepartmentById(departmentId: Int): GetDepartmentByIdResponse {
        return authApiService.getDepartmentById(departmentId)
    }

    companion object {
        @Volatile
        private var instance: DepartmentRepository? = null
        fun getInstance(
            authApiService: ApiService,
            unAuthApiService: ApiService
        ): DepartmentRepository =
            instance ?: synchronized(this) {
                instance ?: DepartmentRepository(authApiService, unAuthApiService)
            }.also { instance = it }
    }
}