package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.repository.UserRepository
import javax.inject.Inject

class SignoutUsecase @Inject constructor(private val userRepository: UserRepository) {

    fun signOut(event: () -> Unit) {
        userRepository.signOut {
            event()
        }
    }
}