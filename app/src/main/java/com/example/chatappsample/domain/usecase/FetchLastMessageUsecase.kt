package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.dto.MessageDomain
import com.example.chatappsample.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class FetchLastMessageUsecase @Inject constructor(private val repo: ChatRepository) {
    suspend operator fun invoke(chatRoom: String): Flow<MessageDomain?> = repo.fetchLastMessageOfChatRoom(chatRoom).map {
        it
    }
}