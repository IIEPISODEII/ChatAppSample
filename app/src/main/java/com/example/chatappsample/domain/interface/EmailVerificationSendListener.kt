package com.example.chatappsample.domain.`interface`

interface EmailVerificationSendListener {
    fun onSuccess()

    fun onSendEmailVerificationFail()

    fun onStart()
}