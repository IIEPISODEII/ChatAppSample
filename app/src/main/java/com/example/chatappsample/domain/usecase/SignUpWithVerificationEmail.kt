package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.`interface`.OnEmailVerificationListener
import com.example.chatappsample.domain.repository.UserRepository
import javax.inject.Inject

class SignUpWithVerifiedEmailUsecase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(name: String = "", listener: OnEmailVerificationListener) {
        userRepository.signUp(name, listener)
    }
}