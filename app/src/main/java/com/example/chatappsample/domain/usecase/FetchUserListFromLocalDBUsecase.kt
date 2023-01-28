package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.dto.UserDomain
import com.example.chatappsample.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FetchUserListFromLocalDBUsecase @Inject constructor(private val userRepository: UserRepository) {

    suspend operator fun invoke(myId: String): Flow<List<UserDomain>> {
        return userRepository.fetchUserListFromLocalDB().map { list ->
            list.filter { user ->
                user.uid != myId
            }
        }
    }

}