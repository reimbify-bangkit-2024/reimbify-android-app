package com.example.reimbifyapp.user.di

import android.content.Context
import com.example.reimbifyapp.user.data.network.api.ApiConfig
import com.example.reimbifyapp.user.data.preferences.UserPreference
import com.example.reimbifyapp.user.data.preferences.dataStore
import com.example.reimbifyapp.user.data.repositories.UserRepository

object Injection {
    fun provideUserRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        return UserRepository.getInstance(pref)
    }
}