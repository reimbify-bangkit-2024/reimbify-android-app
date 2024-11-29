package com.example.reimbifyapp.user.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reimbifyapp.user.data.network.response.UploadResponse
import com.example.reimbifyapp.user.data.repositories.ImageRepository
import kotlinx.coroutines.launch

class AddRequestViewModel(private val imageRepository: ImageRepository) : ViewModel() {

    private val _uploadResponse = MutableLiveData<UploadResponse?>()
    val uploadResponse: LiveData<UploadResponse?> = _uploadResponse

    fun uploadImage(authToken: String, imageUri: Uri, userId: String) {
        viewModelScope.launch {
            try {
                val response = imageRepository.uploadImage(authToken, imageUri, userId)
                _uploadResponse.value = response
            } catch (e: Exception) {
                _uploadResponse.value = null
                _uploadResponse.postValue(null)
            }
        }
    }
}

