package com.example.reimbifyapp.user.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reimbifyapp.data.repositories.ReimbursementRepository
import com.example.reimbifyapp.data.repositories.UserRepository
import com.example.reimbifyapp.di.Injection
import com.example.reimbifyapp.user.viewmodel.DashboardViewModel

class DashboardViewModelFactory (
    private val userRepository: UserRepository,
    private val reimbursementRepository: ReimbursementRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(userRepository, reimbursementRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: DashboardViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context): DashboardViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                val userRepository = Injection.provideUserRepository(context)
                val reimbursementRepository = Injection.provideReimbursementRepository(context)

                DashboardViewModelFactory(userRepository, reimbursementRepository).also { INSTANCE = it }
            }
        }
    }
}