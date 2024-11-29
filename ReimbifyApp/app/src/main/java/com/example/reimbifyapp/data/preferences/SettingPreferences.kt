package com.example.reimbifyapp.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingPreferences private constructor(private val dataStore: DataStore<Preferences>) {

    private val THEME_KEY = booleanPreferencesKey("theme_setting")

    fun getThemeSetting(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[THEME_KEY] ?: false
        }
    }

    suspend fun saveThemeSetting(isDarkModeActive: Boolean) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = isDarkModeActive
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: com.example.reimbifyapp.data.preferences.SettingPreferences? = null

        fun getInstance(dataStore: DataStore<Preferences>): com.example.reimbifyapp.data.preferences.SettingPreferences {
            return com.example.reimbifyapp.data.preferences.SettingPreferences.Companion.INSTANCE ?: synchronized(this) {
                val instance =
                    com.example.reimbifyapp.data.preferences.SettingPreferences(dataStore)
                com.example.reimbifyapp.data.preferences.SettingPreferences.Companion.INSTANCE = instance
                instance
            }
        }
    }

}