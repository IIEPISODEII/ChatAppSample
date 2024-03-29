package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.dto.MessageDomain
import com.example.chatappsample.domain.repository.ChatRepository
import javax.inject.Inject

class FetchMessagesFromLocalDBUsecase @Inject constructor(private val chatRepository: ChatRepository) {

    suspend operator fun invoke(
        chatRoom: String,
        queriesSize: Int,
        offset: Int
    ): List<MessageDomain> {
        return chatRepository.fetchMessagesFromLocalDB(chatRoom, queriesSize, offset)
            .map { it ?: MessageDomain() }
    }
}