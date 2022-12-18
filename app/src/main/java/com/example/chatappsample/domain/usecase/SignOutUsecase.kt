package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.repository.UserRepository
import javax.inject.Inject

class SignOutUsecase @Inject constructor(private val userRepository: UserRepository) {

    fun signOut() : Boolean {
        return userRepository.signOut()
    }
}