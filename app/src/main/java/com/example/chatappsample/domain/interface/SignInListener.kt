package com.example.chatappsample.domain.`interface`

interface SignInListener {
    fun <T: Any?> onSuccess(successParam: T)

    fun onFail(exception: Exception)
}