package com.example.chatappsample.domain.`interface`

import com.example.chatappsample.domain.dto.UserDomain

interface OnEmailVerificationListener {
    fun onSuccess(user: UserDomain)

    fun onFailEmailVerification()

    fun onFail(exception: Exception)
}