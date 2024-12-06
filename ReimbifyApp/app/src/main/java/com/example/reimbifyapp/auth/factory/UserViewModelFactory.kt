package com.example.reimbifyapp.auth.factory

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reimbifyapp.data.repositories.ImageRepository
import com.example.reimbifyapp.data.repositories.UserRepository
import com.example.reimbifyapp.di.Injection
import com.example.reimbifyapp.auth.viewmodel.LoginViewModel
import com.example.reimbifyapp.data.preferences.UserPreference
import com.example.reimbifyapp.data.repositories.DepartmentRepository
import com.example.reimbifyapp.data.repositories.ProfileRepository
import com.example.reimbifyapp.user.viewmodel.AddRequestViewModel
import com.example.reimbifyapp.data.preferences.dataStore
import com.example.reimbifyapp.data.repositories.RequestRepository

class UserViewModelFactory(
    private val userPreferencesDataStore: DataStore<Preferences>,
    private val userRepository: UserRepository?,
    private val imageRepository: ImageRepository?,
    private val profileRepository: ProfileRepository?,
    private val departmentRepository: DepartmentRepository?,
    private val requestRepository: RequestRepository?
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(userRepository!!) as T
            }
            modelClass.isAssignableFrom(AddRequestViewModel::class.java) -> {
                val userPreference = UserPreference.getInstance(userPreferencesDataStore)

                AddRequestViewModel(userPreference, profileRepository!!, imageRepository!!, departmentRepository!!, requestRepository!!) as T
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
                val userPreferencesDataStore = context.dataStore  // Use the correct dataStore here
                val userRepository = Injection.provideUserRepository(context)
                val imageRepository = ImageRepository(context)
                val profileRepository = Injection.provideProfileRepository(context)
                val departmentRepository = Injection.provideDepartmentRepository(context)
                val requestRepository = Injection.provideRequestRepository(context)

                INSTANCE = UserViewModelFactory(userPreferencesDataStore, userRepository, imageRepository, profileRepository, departmentRepository, requestRepository)
                INSTANCE!!
            }
        }
    }
}
