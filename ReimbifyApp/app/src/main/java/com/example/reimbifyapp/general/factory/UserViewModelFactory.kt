package com.example.reimbifyapp.general.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reimbifyapp.data.repositories.ImageRepository
import com.example.reimbifyapp.data.repositories.UserRepository
import com.example.reimbifyapp.di.Injection
import com.example.reimbifyapp.general.viewmodel.LoginViewModel
import com.example.reimbifyapp.user.viewmodel.AddRequestViewModel

class UserViewModelFactory(
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
        private var INSTANCE: UserViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context): UserViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                val userRepository = Injection.provideUserRepository(context)
                val imageRepository = ImageRepository(context) // Provide ImageRepository as well
                INSTANCE = UserViewModelFactory(userRepository, imageRepository)
                INSTANCE!!
            }
        }
    }
}
