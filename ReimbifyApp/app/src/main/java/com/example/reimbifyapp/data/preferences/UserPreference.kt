package com.example.reimbifyapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.reimbifyapp.data.entities.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class UserPreference private constructor(private val dataStore: DataStore<Preferences>) {

    suspend fun saveSession(userSession: UserSession) {
        dataStore.edit { preferences ->
            preferences[com.example.reimbifyapp.data.preferences.UserPreference.Companion.USER_ID_KEY] = userSession.userId
            preferences[com.example.reimbifyapp.data.preferences.UserPreference.Companion.TOKEN_KEY] = userSession.token
            preferences[com.example.reimbifyapp.data.preferences.UserPreference.Companion.ROLE_KEY] = userSession.role
            preferences[com.example.reimbifyapp.data.preferences.UserPreference.Companion.IS_LOGIN_KEY] = true
        }
    }

    fun getSession(): Flow<UserSession> {
        return dataStore.data.map { preferences ->
            UserSession(
                preferences[com.example.reimbifyapp.data.preferences.UserPreference.Companion.USER_ID_KEY] ?: "",
                preferences[com.example.reimbifyapp.data.preferences.UserPreference.Companion.TOKEN_KEY] ?: "",
                preferences[com.example.reimbifyapp.data.preferences.UserPreference.Companion.ROLE_KEY] ?: "",
                preferences[com.example.reimbifyapp.data.preferences.UserPreference.Companion.IS_LOGIN_KEY] ?: false
            )
        }
    }

    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: com.example.reimbifyapp.data.preferences.UserPreference? = null

        private val USER_ID_KEY = stringPreferencesKey("userId")
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val ROLE_KEY = stringPreferencesKey("role")
        private val IS_LOGIN_KEY = booleanPreferencesKey("isLogin")

        fun getInstance(dataStore: DataStore<Preferences>): com.example.reimbifyapp.data.preferences.UserPreference {
            return com.example.reimbifyapp.data.preferences.UserPreference.Companion.INSTANCE ?: synchronized(this) {
                val instance = com.example.reimbifyapp.data.preferences.UserPreference(dataStore)
                com.example.reimbifyapp.data.preferences.UserPreference.Companion.INSTANCE = instance
                instance
            }
        }
    }
}