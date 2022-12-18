package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.`interface`.OnEmailVerificationListener
import com.example.chatappsample.domain.`interface`.OnSignInListener
import com.example.chatappsample.domain.repository.UserRepository
import javax.inject.Inject

class SignUpUsecase @Inject constructor(private val repo: UserRepository) {
    operator fun invoke(name: String, listener: OnEmailVerificationListener) {
        repo.signUp(name, listener)
    }
}