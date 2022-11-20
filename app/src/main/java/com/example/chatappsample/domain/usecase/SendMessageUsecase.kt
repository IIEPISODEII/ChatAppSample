package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.`interface`.OnFirebaseCommunicationListener
import com.example.chatappsample.domain.dto.MessageDomain
import com.example.chatappsample.domain.repository.ChatRepository
import javax.inject.Inject

class SendMessageUsecase @Inject constructor(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(
        message: MessageDomain,
        chatRoom: String,
        onFirebaseCommunicationListener: OnFirebaseCommunicationListener
    ) {
        chatRepository.sendMessage(
            message = message,
            chatRoom = chatRoom,
            onFirebaseCommunicationListener = onFirebaseCommunicationListener
        )
    }
}