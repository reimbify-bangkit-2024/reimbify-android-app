package com.example.reimbifyapp.user.utils

import android.util.Log
import androidx.datastore.core.IOException
import org.json.JSONObject
import retrofit2.HttpException

object ErrorUtils {
    fun parseErrorMessage(throwable: Throwable): String {
        return try {
            if (throwable is HttpException) {
                val errorBody = throwable.response()?.errorBody()?.string()
                if (!errorBody.isNullOrEmpty()) {
                    val jsonObject = JSONObject(errorBody)
                    return jsonObject.optString("message", "An unknown error occurred")
                }
            } else if (throwable is IOException) {
                return "Network error occurred. Please check your connection."
            }

            throwable.localizedMessage ?: "An unknown error occurred"
        } catch (e: Exception) {
            Log.e("PARSE ERROR", "Error parsing throwable", e)
            "An unknown error occurred"
        }
    }
}
