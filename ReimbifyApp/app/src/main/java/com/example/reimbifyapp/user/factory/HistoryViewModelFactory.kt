package com.example.reimbifyapp.user.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reimbifyapp.user.viewmodel.HistoryViewModel
import com.example.reimbifyapp.data.repositories.DepartmentRepository
import com.example.reimbifyapp.data.repositories.ReimbursementRepository
import com.example.reimbifyapp.data.repositories.UserRepository
import com.example.reimbifyapp.di.Injection

class HistoryViewModelFactory (
    private val userRepository: UserRepository,
    private val reimbursementRepository: ReimbursementRepository,
    private val departmentRepository: DepartmentRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(userRepository, reimbursementRepository, departmentRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: HistoryViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context): HistoryViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                val userRepository = Injection.provideUserRepository(context)
                val reimbursementRepository = Injection.provideReimbursementRepository(context)
                val departmentRepository = Injection.provideDepartmentRepository(context)

                HistoryViewModelFactory(userRepository, reimbursementRepository, departmentRepository).also { INSTANCE = it }
            }
        }
    }
}
