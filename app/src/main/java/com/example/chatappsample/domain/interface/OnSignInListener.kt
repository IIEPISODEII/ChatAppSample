package com.example.chatappsample.domain.`interface`

interface OnSignInListener {
    fun <T: Any?> onSuccess(successParam: T)

    fun onFail(exception: Exception)
}