package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.`interface`.EmailVerificationSendListener
import com.example.chatappsample.domain.repository.UserRepository
import javax.inject.Inject

class SendEmailVerificationUsecase @Inject constructor(
    private val userRepository: UserRepository
) {

    // If user id is created successfully, this returns true
    operator fun invoke(email: String, password: String, listener: EmailVerificationSendListener) {
        userRepository.sendVerificationEmail(email, password, listener)
    }
}