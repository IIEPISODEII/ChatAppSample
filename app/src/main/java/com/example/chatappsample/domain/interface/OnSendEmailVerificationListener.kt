package com.example.chatappsample.domain.`interface`

interface OnSendEmailVerificationListener {
    fun onSuccess()

    fun onSendEmailVerificationFail()

    fun onStart()

    fun <T : Any?> onFailure(error: T)
}