package com.example.reimbifyapp.admin.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.reimbifyapp.data.network.api.ApiConfig
import com.example.reimbifyapp.data.network.response.GetUserResponse
import com.example.reimbifyapp.data.network.response.UploadResponse
import com.example.reimbifyapp.data.preferences.SettingPreferences
import com.example.reimbifyapp.data.preferences.UserPreference
import com.example.reimbifyapp.data.repositories.ImageRepository
import com.example.reimbifyapp.data.repositories.ProfileRepository
import kotlinx.coroutines.launch

class SettingViewModel(
    private val pref: SettingPreferences,
    private val repository: ProfileRepository,
    private val imageRepository: ImageRepository,
    private val userPreference: UserPreference
) : ViewModel() {

    private val _getUserResult = MutableLiveData<Result<GetUserResponse>>()
    val getUserResult: LiveData<Result<GetUserResponse>> = _getUserResult

    private val _uploadResponse = MutableLiveData<UploadResponse?>()
    val uploadResponse: LiveData<UploadResponse?> = _uploadResponse

    fun getThemeSettings(): LiveData<Boolean> {
        return pref.getThemeSetting().asLiveData()
    }

    fun saveThemeSetting(isDarkModeActive: Boolean) {
        viewModelScope.launch {
            Log.d("THEME DARK", isDarkModeActive.toString())
            pref.saveThemeSetting(isDarkModeActive)
        }
    }

    fun getUser(userId: String) {
        viewModelScope.launch {
            try {
                val response = repository.getUserInfo(userId)
                _getUserResult.postValue(Result.success(response))
            } catch (e: Exception) {
                _getUserResult.postValue(Result.failure(e))
            }
        }
    }

    fun uploadImage(imageUri: Uri, userId: String){
        viewModelScope.launch {
            try {
                val apiService = ApiConfig.createAuthenticatedApiService(userPreference)
                val response = imageRepository.uploadImageProfile(apiService, imageUri, userId)
                _uploadResponse.value = response
            } catch (e: Exception) {
                Log.e("AddRequestViewModel", "Upload image failed: ${e.message}", e)
                _uploadResponse.value = null
            }
        }
    }
}
