package com.example.reimbifyapp.data.network.api

import com.example.reimbifyapp.BuildConfig
import com.example.reimbifyapp.data.network.AuthorizationInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {

    private const val BASE_URL = BuildConfig.BASE_URL

    private fun createRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun createClient(withAuth: Boolean, userPreference: com.example.reimbifyapp.data.preferences.UserPreference? = null): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val builder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        if (withAuth && userPreference != null) {
            val authInterceptor = AuthorizationInterceptor(userPreference)
            builder.addInterceptor(authInterceptor)
        }

        return builder.build()
    }

    fun createAuthenticatedApiService(userPreference: com.example.reimbifyapp.data.preferences.UserPreference): com.example.reimbifyapp.data.network.api.ApiService {
        val client = createClient(withAuth = true, userPreference = userPreference)
        return createRetrofit(client).create(com.example.reimbifyapp.data.network.api.ApiService::class.java)
    }

    fun createUnauthenticatedApiService(): com.example.reimbifyapp.data.network.api.ApiService {
        val client = createClient(withAuth = false)
        return createRetrofit(client).create(com.example.reimbifyapp.data.network.api.ApiService::class.java)
    }

    val apiService: com.example.reimbifyapp.data.network.api.ApiService by lazy {
        createUnauthenticatedApiService()
    }
}