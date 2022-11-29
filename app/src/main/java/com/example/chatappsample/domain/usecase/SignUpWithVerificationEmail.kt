package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.`interface`.OnEmailVerificationListener
import com.example.chatappsample.domain.`interface`.OnSendEmailVerificationListener
import com.example.chatappsample.domain.repository.UserRepository
import javax.inject.Inject

class SignUpWithVerifiedEmail @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(listener: OnEmailVerificationListener) {
        userRepository.signUpWithVerifiedEmail(listener)
    }
}