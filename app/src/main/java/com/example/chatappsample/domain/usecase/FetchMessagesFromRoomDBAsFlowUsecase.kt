package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.dto.Message
import com.example.chatappsample.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FetchMessagesFromRoomDBAsFlowUsecase @Inject constructor(private val chatRepository: ChatRepository) {

    suspend operator fun invoke(chatRoom: String, queriesSize: Int) : Flow<List<Message>> {
        return chatRepository.fetchMessagesFromRoomDBAsFlow(chatRoom, queriesSize)
    }
}