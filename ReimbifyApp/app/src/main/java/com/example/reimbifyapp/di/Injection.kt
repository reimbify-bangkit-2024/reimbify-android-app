package com.example.reimbifyapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.reimbifyapp.data.network.api.ApiConfig
import com.example.reimbifyapp.data.preferences.dataStore
import com.example.reimbifyapp.data.repositories.DepartmentRepository
import com.example.reimbifyapp.data.repositories.ProfileRepository
import com.example.reimbifyapp.data.repositories.ReimbursementRepository
import com.example.reimbifyapp.data.repositories.RequestRepository
import com.example.reimbifyapp.data.repositories.UserRepository

object Injection {
    private fun provideAuthenticatedApiService(context: Context): com.example.reimbifyapp.data.network.api.ApiService {
        val userPreference = com.example.reimbifyapp.data.preferences.UserPreference.getInstance(context.dataStore)
        return ApiConfig.createAuthenticatedApiService(userPreference)
    }

    private fun provideUnauthenticatedApiService(): com.example.reimbifyapp.data.network.api.ApiService {
        return ApiConfig.createUnauthenticatedApiService()
    }

    fun provideUserPreference(context: Context): com.example.reimbifyapp.data.preferences.UserPreference {
        return com.example.reimbifyapp.data.preferences.UserPreference.getInstance(context.dataStore)
    }

    fun provideDataStore(context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    fun provideUserRepository(context: Context): UserRepository {
        val authApiService = provideAuthenticatedApiService(context)
        val unAuthApiService = provideUnauthenticatedApiService()

        val pref = com.example.reimbifyapp.data.preferences.UserPreference.getInstance(context.dataStore)
        return UserRepository.getInstance(pref, authApiService, unAuthApiService)
    }

    fun provideProfileRepository(context: Context): ProfileRepository {
        val authApiService = provideAuthenticatedApiService(context)
        val unAuthApiService = provideUnauthenticatedApiService()

        return ProfileRepository.getInstance(authApiService, unAuthApiService)
    }

    fun provideReimbursementRepository(context: Context): ReimbursementRepository {
        val authApiService = provideAuthenticatedApiService(context)
        val unAuthApiService = provideUnauthenticatedApiService()

        return ReimbursementRepository.getInstance(authApiService, unAuthApiService)
    }

    fun provideDepartmentRepository(context: Context): DepartmentRepository {
        val authApiService = provideAuthenticatedApiService(context)
        val unAuthApiService = provideUnauthenticatedApiService()

        return DepartmentRepository.getInstance(authApiService, unAuthApiService)
    }

    fun provideRequestRepository(context: Context): RequestRepository {
        val authApiService = provideAuthenticatedApiService(context)
        val unAuthApiService = provideUnauthenticatedApiService()

        return RequestRepository.getInstance(authApiService, unAuthApiService)
    }
}
