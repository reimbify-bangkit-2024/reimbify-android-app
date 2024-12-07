package com.example.reimbifyapp.data.repositories

import android.content.Context
import android.net.Uri
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
