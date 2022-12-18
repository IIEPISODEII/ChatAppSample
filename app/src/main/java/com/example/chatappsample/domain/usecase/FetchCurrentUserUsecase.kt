package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.`interface`.OnGetDataListener
import com.example.chatappsample.domain.repository.UserRepository
import javax.inject.Inject

class FetchCurrentUserUsecase @Inject constructor (private val userRepository: UserRepository) {

    operator fun invoke(listener: OnGetDataListener) {
        userRepository.fetchCurrentUser(listener)
    }
}