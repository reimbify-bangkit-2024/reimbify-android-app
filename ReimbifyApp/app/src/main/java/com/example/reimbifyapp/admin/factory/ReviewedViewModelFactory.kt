package com.example.reimbifyapp.admin.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reimbifyapp.admin.viewmodel.ReviewedViewModel
import com.example.reimbifyapp.data.repositories.DepartmentRepository
import com.example.reimbifyapp.data.repositories.ReimbursementRepository
import com.example.reimbifyapp.di.Injection

class ReviewedViewModelFactory (
    private val reimbursementRepository: ReimbursementRepository,
    private val departmentRepository: DepartmentRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ReviewedViewModel::class.java) -> {
                ReviewedViewModel(reimbursementRepository, departmentRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ReviewedViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context): ReviewedViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                val reimbursementRepository = Injection.provideReimbursementRepository(context)
                val departmentRepository = Injection.provideDepartmentRepository(context)

                ReviewedViewModelFactory(reimbursementRepository, departmentRepository).also { INSTANCE = it }
            }
        }
    }
}
