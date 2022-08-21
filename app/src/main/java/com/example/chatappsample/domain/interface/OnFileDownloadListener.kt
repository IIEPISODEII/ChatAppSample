package com.example.chatappsample.domain.`interface`

import android.net.Uri
import java.lang.Exception

interface OnFileDownloadListener {
    fun onSuccess(uri: Uri)

    fun onFailure(e: Exception)
}