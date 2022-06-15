package com.example.chatappsample.domain.usecase

import androidx.lifecycle.MutableLiveData
import com.example.chatappsample.domain.repository.UserRepository
import javax.inject.Inject

class SignUpUsecase @Inject constructor(
    private val userRepository: UserRepository
) {

    // If user id is created successfully, this returns true
    fun signUp(name: String, email: String, password: String): Boolean {
        return userRepository.signUp(name, email, password)
    }
}