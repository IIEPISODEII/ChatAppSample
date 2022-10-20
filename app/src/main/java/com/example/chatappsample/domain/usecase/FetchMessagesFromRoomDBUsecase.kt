package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.dto.Message
import com.example.chatappsample.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FetchMessagesFromRoomDBUsecase @Inject constructor(private val chatRepository: ChatRepository) {

    suspend operator fun invoke(chatRoom: String, queriesSize: Int, offset: Int) : List<Message> {
        return chatRepository.fetchMessagesFromRoomDB(chatRoom, queriesSize, offset)
    }
}