package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.`interface`.SignInListener
import com.example.chatappsample.domain.repository.UserRepository
import javax.inject.Inject

class SignInUsecase @Inject constructor(private val repo: UserRepository) {
    operator fun invoke(email: String, password: String, listener: SignInListener) {
        repo.signIn(email, password, listener)
    }
}