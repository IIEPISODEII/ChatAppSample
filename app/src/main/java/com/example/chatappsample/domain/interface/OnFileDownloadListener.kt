package com.example.chatappsample.domain.`interface`

interface OnFileDownloadListener {
    fun onSuccess(byteArray: ByteArray)

    fun onFail(e: Exception)
}