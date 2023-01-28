package com.example.chatappsample.domain.`interface`

interface FileDownloadListener {
    fun onSuccess(byteArray: ByteArray)

    fun onFail(e: Exception)
}