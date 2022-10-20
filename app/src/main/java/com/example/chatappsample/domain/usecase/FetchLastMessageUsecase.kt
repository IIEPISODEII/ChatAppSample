package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.`interface`.OnGetDataListener
import com.example.chatappsample.domain.dto.Message
import com.example.chatappsample.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FetchLastMessageUsecase @Inject constructor(private val repo: ChatRepository) {
    suspend operator fun invoke(chatRoom: String): Flow<Message> = repo.takeLastMessageOfChatRoom(chatRoom)
}