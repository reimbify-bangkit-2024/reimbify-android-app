package com.example.reimbifyapp.admin.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reimbifyapp.admin.viewmodel.UserListViewModel
import com.example.reimbifyapp.data.repositories.DepartmentRepository
import com.example.reimbifyapp.data.repositories.UserRepository
import com.example.reimbifyapp.di.Injection

class UserListViewModelFactory (
    private val userRepository: UserRepository,
    private val departmentRepository: DepartmentRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(UserListViewModel::class.java) -> {
                UserListViewModel(userRepository, departmentRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserListViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context): UserListViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                val userRepository = Injection.provideUserRepository(context)
                val departmentRepository = Injection.provideDepartmentRepository(context)

                UserListViewModelFactory(userRepository, departmentRepository).also { INSTANCE = it }
            }
        }
    }
}