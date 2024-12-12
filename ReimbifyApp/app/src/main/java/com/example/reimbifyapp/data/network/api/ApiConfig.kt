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
    private const val BASE_URL_MODEL = BuildConfig.BASE_URL_MODEL

    private fun createRetrofit(client: OkHttpClient, baseUrl: String = BASE_URL): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
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

    fun createAuthenticatedApiService(userPreference: com.example.reimbifyapp.data.preferences.UserPreference): ApiService {
        val client = createClient(withAuth = true, userPreference = userPreference)
        return createRetrofit(client).create(ApiService::class.java)
    }

    fun createUnauthenticatedApiService(): ApiService {
        val client = createClient(withAuth = false)
        return createRetrofit(client).create(ApiService::class.java)
    }

    fun createModelApiService(): ApiService {
        val client = createClient(withAuth = false)
        return createRetrofit(client, BASE_URL_MODEL.toString()).create(ApiService::class.java)
    }

    val apiService: ApiService by lazy {
        createUnauthenticatedApiService()
    }
}
