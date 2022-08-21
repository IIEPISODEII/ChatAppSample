package com.example.chatappsample.util

import android.content.Context
import androidx.core.content.getSystemService
import java.lang.Exception

inline fun <T> safeCall(action: () -> Resource<T>): Resource<T> {
    return try {
        action()
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Unknown Error occurred.")
    }
}

fun convertDPtoPX(context: Context, dp: Int) : Float {
    return context.resources.displayMetrics.density * dp
}

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Loading<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}