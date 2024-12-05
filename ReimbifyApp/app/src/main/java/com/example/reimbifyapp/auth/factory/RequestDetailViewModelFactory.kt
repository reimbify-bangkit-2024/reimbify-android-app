package com.example.reimbifyapp.auth.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reimbifyapp.auth.viewmodel.RequestDetailViewModel
import com.example.reimbifyapp.data.repositories.ReimbursementRepository
import com.example.reimbifyapp.di.Injection

class RequestDetailViewModelFactory(
    private val reimbursementRepository: ReimbursementRepository
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(RequestDetailViewModel::class.java) -> {
                RequestDetailViewModel(reimbursementRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: RequestDetailViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context): RequestDetailViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                val reimbursementRepository = Injection.provideReimbursementRepository(context)

                INSTANCE = RequestDetailViewModelFactory(reimbursementRepository)
                INSTANCE!!
            }
        }
    }
}