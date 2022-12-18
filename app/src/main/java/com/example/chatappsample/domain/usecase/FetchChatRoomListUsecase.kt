package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.dto.ChatRoomDomain
import com.example.chatappsample.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FetchChatRoomListUsecase @Inject constructor (private val userRepository: UserRepository) {

    suspend operator fun invoke(currentUserId: String): Flow<List<ChatRoomDomain>> {
        return userRepository.fetchChatRoomList(currentUserId)
    }
}