package com.example.chatappsample.util

import android.content.Context
import androidx.core.content.getSystemService
import java.lang.Exception

fun convertDPtoPX(context: Context, dp: Int) : Float {
    return context.resources.displayMetrics.density * dp
}

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Loading<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}

val TEN_MEGABYTE = 10L*1024L*1024L

val FULL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"
val COERCE_DATE_FORMAT = "yyyyMMddHHmmss"