package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.dto.User
import com.example.chatappsample.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ReceiveAllUsersFromExternalDBUsecase @Inject constructor(private val userRepository: UserRepository) {

    operator fun invoke(coroutineScope: CoroutineScope) {
        userRepository.receiveAllUsersFromExternalDB(coroutineScope)
    }

}