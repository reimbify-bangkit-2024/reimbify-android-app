package com.example.reimbifyapp.auth.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reimbifyapp.auth.viewmodel.RequestApprovalViewModel
import com.example.reimbifyapp.data.repositories.ReimbursementRepository
import com.example.reimbifyapp.di.Injection

class RequestApprovalViewModelFactory (
    private val reimbursementRepository: ReimbursementRepository
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(RequestApprovalViewModel::class.java) -> {
                RequestApprovalViewModel(reimbursementRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: RequestApprovalViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context): RequestApprovalViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                val reimbursementRepository = Injection.provideReimbursementRepository(context)

                INSTANCE = RequestApprovalViewModelFactory(reimbursementRepository)
                INSTANCE!!
            }
        }
    }
}