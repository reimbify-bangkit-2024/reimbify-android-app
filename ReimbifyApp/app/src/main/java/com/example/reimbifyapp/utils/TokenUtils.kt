package com.example.reimbifyapp.utils

import android.util.Base64
import android.util.Log
import org.json.JSONObject

object TokenUtils {
    fun isTokenValid(token: String): Boolean {
        val currentTime = System.currentTimeMillis() / 1000
        val tokenExpiryTime = decodeTokenExpiry(token)

        return currentTime < tokenExpiryTime
    }

    fun decodeTokenExpiry(token: String): Long {
        try {
            val parts = token.split(".")
            val payload = parts[1]
            val decodedPayload = String(Base64.decode(payload, Base64.URL_SAFE))
            val jsonObject = JSONObject(decodedPayload)
            return jsonObject.getLong("exp")
        } catch (e: Exception) {
            Log.e("TOKEN", "Failed to decode token: ${e.message}")
            return 0L
        }
    }
}