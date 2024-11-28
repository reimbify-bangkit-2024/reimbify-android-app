package com.example.reimbifyapp.user.di

import android.content.Context
import com.example.reimbifyapp.user.data.network.api.ApiConfig
import com.example.reimbifyapp.user.data.network.api.ApiService
import com.example.reimbifyapp.user.data.preferences.UserPreference
import com.example.reimbifyapp.user.data.preferences.dataStore
import com.example.reimbifyapp.user.data.repositories.ProfileRepository
import com.example.reimbifyapp.user.data.repositories.UserRepository

object Injection {
    private fun provideAuthenticatedApiService(context: Context): ApiService {
        val userPreference = UserPreference.getInstance(context.dataStore)
        return ApiConfig.createAuthenticatedApiService(userPreference)
    }

    private fun provideUnauthenticatedApiService(): ApiService {
        return ApiConfig.createUnauthenticatedApiService()
    }

    fun provideUserPreference(context: Context): UserPreference {
        return UserPreference.getInstance(context.dataStore)
    }

    fun provideUserRepository(context: Context): UserRepository {
        val authApiService = provideAuthenticatedApiService(context)
        val unAuthApiService = provideUnauthenticatedApiService()

        val pref = UserPreference.getInstance(context.dataStore)
        return UserRepository.getInstance(pref, authApiService, unAuthApiService)
    }

    fun provideProfileRepository(context: Context): ProfileRepository {
        val authApiService = provideAuthenticatedApiService(context)
        val unAuthApiService = provideUnauthenticatedApiService()

        return ProfileRepository.getInstance(authApiService, unAuthApiService)
    }
}
