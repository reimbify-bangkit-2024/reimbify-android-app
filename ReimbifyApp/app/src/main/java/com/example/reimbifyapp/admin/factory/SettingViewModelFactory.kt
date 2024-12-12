package com.example.reimbifyapp.admin.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reimbifyapp.admin.viewmodel.SettingViewModel
import com.example.reimbifyapp.data.preferences.SettingPreferences
import com.example.reimbifyapp.data.preferences.UserPreference
import com.example.reimbifyapp.data.preferences.dataStore
import com.example.reimbifyapp.data.repositories.ImageRepository
import com.example.reimbifyapp.data.repositories.ProfileRepository
import com.example.reimbifyapp.di.Injection

class SettingViewModelFactory(
    private val pref: SettingPreferences,
    private val userRepository: ProfileRepository,
    private val imageRepository: ImageRepository,
    private val userPreference: UserPreference
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SettingViewModel::class.java) -> {
                SettingViewModel(pref, userRepository, imageRepository, userPreference) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SettingViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context): SettingViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                val dataStore = Injection.provideDataStore(context)
                val preferences = SettingPreferences.getInstance(dataStore)
                val profileRepository = Injection.provideProfileRepository(context)
                val imageRepository = ImageRepository(context)
                val userPreference = UserPreference.getInstance(context.dataStore)
                INSTANCE = SettingViewModelFactory(preferences, profileRepository, imageRepository, userPreference)
                INSTANCE!!
            }
        }
    }
}