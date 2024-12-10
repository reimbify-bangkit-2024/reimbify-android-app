package com.example.reimbifyapp.data.repositories

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.reimbifyapp.admin.ui.component.ApprovalConfirmationDialog.Companion.TAG
import com.example.reimbifyapp.data.network.api.ApiConfig.createModelApiService
import com.example.reimbifyapp.data.network.api.ApiService
import com.example.reimbifyapp.data.network.response.PredictionResponse
import com.example.reimbifyapp.data.network.response.UploadResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.InputStream

class ImageRepository(private val context: Context) {
    suspend fun uploadImage(apiService: ApiService, imageUri: Uri, userId: String): UploadResponse {
        return withContext(Dispatchers.IO) {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val tempFile = createTempFileFromInputStream(inputStream)
            val requestBody = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("receiptImage", tempFile.name, requestBody)
            return@withContext apiService.uploadReceiptImage(imagePart, userId)
        }
    }

    suspend fun uploadImageProfile(apiService: ApiService, imageUri: Uri, userId: String): UploadResponse {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                if (inputStream == null) {
                    Log.e(TAG, "Failed to open input stream for image URI: $imageUri")
                    throw Exception("Input stream is null")
                }
                val tempFile = createTempFileFromInputStream(inputStream)
                Log.d(TAG, "Temporary file created at: ${tempFile.absolutePath}")
                val requestBody = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("profileImage", tempFile.name, requestBody)
                Log.d(TAG, "Uploading profile image to server for userId: $userId")
                val response = apiService.uploadImage(imagePart, userId)
                Log.d(TAG, "Server response: $response")
                return@withContext response
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading profile image: ${e.message}", e)
                throw e
            }
        }
    }

    private fun createTempFileFromInputStream(inputStream: InputStream?): File {
        val tempFile = File.createTempFile("tempImage", ".jpg", context.cacheDir)
        val outputStream = tempFile.outputStream()
        inputStream?.copyTo(outputStream)
        outputStream.close()
        inputStream?.close()
        return tempFile
    }

    suspend fun predictImage(imageUri: Uri): PredictionResponse {
        val apiService = createModelApiService()
        return withContext(Dispatchers.IO) {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val tempFile = createTempFileFromInputStream(inputStream)
            val requestBody = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", tempFile.name, requestBody)
            return@withContext apiService.predictImage(imagePart)
        }
    }

}
