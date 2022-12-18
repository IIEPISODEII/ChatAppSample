package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.`interface`.OnSignInListener
import com.example.chatappsample.domain.repository.UserRepository
import javax.inject.Inject

class SignInUsecase @Inject constructor(private val repo: UserRepository) {
    operator fun invoke(email: String, password: String, listener: OnSignInListener) {
        repo.signIn(email, password, listener)
    }
}