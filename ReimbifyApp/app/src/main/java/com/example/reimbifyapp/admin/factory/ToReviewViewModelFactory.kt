package com.example.reimbifyapp.admin.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reimbifyapp.admin.viewmodel.ToReviewViewModel
import com.example.reimbifyapp.data.repositories.DepartmentRepository
import com.example.reimbifyapp.data.repositories.ReimbursementRepository
import com.example.reimbifyapp.di.Injection

class ToReviewViewModelFactory (
    private val reimbursementRepository: ReimbursementRepository,
    private val departmentRepository: DepartmentRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ToReviewViewModel::class.java) -> {
                ToReviewViewModel(reimbursementRepository, departmentRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ToReviewViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context): ToReviewViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                val reimbursementRepository = Injection.provideReimbursementRepository(context)
                val departmentRepository = Injection.provideDepartmentRepository(context)

                ToReviewViewModelFactory(reimbursementRepository, departmentRepository).also { INSTANCE = it }
            }
        }
    }
}
