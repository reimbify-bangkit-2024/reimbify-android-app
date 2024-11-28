package com.example.reimbifyapp.user.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reimbifyapp.user.data.repositories.ImageRepository
import com.example.reimbifyapp.user.data.repositories.UserRepository
import com.example.reimbifyapp.user.di.Injection

class ViewModelFactory(
    private val userRepository: UserRepository?,
    private val imageRepository: ImageRepository?
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(userRepository!!) as T
            }
            modelClass.isAssignableFrom(AddRequestViewModel::class.java) -> {
                AddRequestViewModel(imageRepository!!) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context): ViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                val userRepository = Injection.provideUserRepository(context)
                val imageRepository = ImageRepository(context) // Provide ImageRepository as well
                INSTANCE = ViewModelFactory(userRepository, imageRepository)
                INSTANCE!!
            }
        }
    }
}
