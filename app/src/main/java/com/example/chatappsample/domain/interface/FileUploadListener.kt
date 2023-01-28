package com.example.chatappsample.domain.`interface`

/** 파이어베이스 통신 중 발생하는 리스너 **/
interface FileUploadListener {
    fun onSuccess()

    fun onFailure()
}