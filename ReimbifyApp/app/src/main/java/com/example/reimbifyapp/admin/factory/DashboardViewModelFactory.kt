package com.example.reimbifyapp.admin.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reimbifyapp.admin.viewmodel.DashboardViewModel
import com.example.reimbifyapp.data.repositories.ReimbursementRepository
import com.example.reimbifyapp.di.Injection

class DashboardViewModelFactory(
    private val reimbursementRepository: ReimbursementRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(reimbursementRepository) as T
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
                val reimbursementRepository = Injection.provideReimbursementRepository(context)
                DashboardViewModelFactory(reimbursementRepository).also { INSTANCE = it }
            }
        }
    }
}
