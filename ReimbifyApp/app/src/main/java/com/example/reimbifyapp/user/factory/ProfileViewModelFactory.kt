package com.example.reimbifyapp.user.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reimbifyapp.user.data.repositories.ProfileRepository
import com.example.reimbifyapp.user.di.Injection
import com.example.reimbifyapp.user.viewmodel.ProfileViewModel

class ProfileViewModelFactory(
    private val profileRepository: ProfileRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(profileRepository) as T
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
                INSTANCE = ProfileViewModelFactory(profileRepository)
                INSTANCE!!
            }
        }
    }
}
