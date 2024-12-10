package com.example.reimbifyapp.user.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reimbifyapp.data.preferences.UserPreference
import com.example.reimbifyapp.data.preferences.dataStore
import com.example.reimbifyapp.data.repositories.ImageRepository
import com.example.reimbifyapp.data.repositories.ProfileRepository
import com.example.reimbifyapp.di.Injection
import com.example.reimbifyapp.user.viewmodel.ProfileViewModel

class ProfileViewModelFactory(
    private val profileRepository: ProfileRepository,
    private val imageRepository: ImageRepository,
    private val userPreference: UserPreference
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(profileRepository, imageRepository, userPreference) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ProfileViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context): ProfileViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                val profileRepository = Injection.provideProfileRepository(context)
                val imageRepository = ImageRepository(context)
                val userPreference = UserPreference.getInstance(context.dataStore)
                INSTANCE = ProfileViewModelFactory(profileRepository, imageRepository, userPreference)
                INSTANCE!!
            }
        }
    }
}
