package com.example.chatappsample.domain.`interface`

interface OnEmailVerificationListener {
    fun onSuccess()

    fun onFailEmailVerification()

    fun onFail(exception: Exception)
}