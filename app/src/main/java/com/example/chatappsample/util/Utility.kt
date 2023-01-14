package com.example.chatappsample.util

import android.content.Context
import androidx.core.content.getSystemService
import java.lang.Exception

fun convertDPtoPX(context: Context, dp: Int) : Float {
    return context.resources.displayMetrics.density * dp
}

fun convertSimpleDateFormatToTime(sdf: String): Array<String> {
    val dateToRead = sdf.substring(0, if (sdf.lastIndex >= 10) 10 else sdf.lastIndex).split('-').joinToString(".") { it.toInt().toString() }
    val time = sdf.substring(11, 16).split(':').map { it.toInt().toString() }
    val timeToRead = (if (time[0].toInt() < 12) "오전 " + (if (time[0] != "0") time[0] else "12") else "오후 " + (if (time[0] != "12") (time[0].toInt()-12).toString() else "12")) + ":" + time[1].padStart(2, '0')
    return arrayOf(dateToRead, timeToRead)
}

sealed class Resource<out T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Loading<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}

val TEN_MEGABYTE = 10L*1024L*1024L

val FULL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"
val COERCE_DATE_FORMAT = "yyyyMMddHHmmss"