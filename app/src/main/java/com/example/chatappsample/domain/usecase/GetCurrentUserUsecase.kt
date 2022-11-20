package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.`interface`.OnGetDataListener
import com.example.chatappsample.domain.repository.UserRepository
import javax.inject.Inject

class GetCurrentUserUsecase @Inject constructor (private val userRepository: UserRepository) {

    fun getCurrentUser(listener: OnGetDataListener) {
        userRepository.getCurrentUser(listener)
    }
}