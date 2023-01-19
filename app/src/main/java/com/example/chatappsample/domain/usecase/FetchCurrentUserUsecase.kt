package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.dto.UserDomain
import com.example.chatappsample.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FetchCurrentUserUsecase @Inject constructor(private val userRepository: UserRepository) {
    operator fun invoke(uid: String): Flow<UserDomain> {
        return userRepository.fetchUserByIdAsFlow(uid)
    }
}