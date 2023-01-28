package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.`interface`.EmailVerifyListener
import com.example.chatappsample.domain.repository.UserRepository
import javax.inject.Inject

class SignUpUsecase @Inject constructor(private val repo: UserRepository) {
    operator fun invoke(name: String, listener: EmailVerifyListener) {
        repo.signUp(name, listener)
    }
}