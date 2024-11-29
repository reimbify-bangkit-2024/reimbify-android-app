package com.example.reimbifyapp.data.repositories

import android.content.Context
import android.net.Uri
import com.example.reimbifyapp.data.network.api.ApiConfig
import com.example.reimbifyapp.data.network.response.UploadResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.InputStream

class ImageRepository(private val context: Context) {

    suspend fun uploadImage(authToken: String, imageUri: Uri, userId: String): UploadResponse {
        return withContext(Dispatchers.IO) {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val tempFile = createTempFileFromInputStream(inputStream)
            val requestBody: RequestBody = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("receiptImage", tempFile.name, requestBody)
            return@withContext ApiConfig.apiService.uploadReceiptImage(authToken, imagePart, userId)
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


}
