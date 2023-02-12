package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.dto.UserDomain
import com.example.chatappsample.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FetchUserInfoFromLocalDBUsecase @Inject constructor(
    private val userRepo: UserRepository
) {
    operator fun invoke(currentUserId: String): Flow<UserDomain> {
        return userRepo.fetchUserByIdAsFlow(currentUserId)
    }
}