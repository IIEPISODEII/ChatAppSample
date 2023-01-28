package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class FetchUserListFromRemoteDBUsecase @Inject constructor(private val userRepository: UserRepository) {

    operator fun invoke(coroutineScope: CoroutineScope) {
        userRepository.fetchUserListFromExternalDB(coroutineScope)
    }

}