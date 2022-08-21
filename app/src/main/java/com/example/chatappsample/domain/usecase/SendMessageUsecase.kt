package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.`interface`.OnFirebaseCommunicationListener
import com.example.chatappsample.domain.dto.Message
import com.example.chatappsample.domain.repository.ChatRepository
import javax.inject.Inject

class SendMessageUsecase @Inject constructor(private val chatRepository: ChatRepository) {
    fun sendMessage(
        message: Message,
        senderChatRoom: String,
        receiverChatRoom: String,
        onFirebaseCommunicationListener: OnFirebaseCommunicationListener
    ) : Boolean {
        return chatRepository.sendMessage(
            message = message,
            senderChatRoom = senderChatRoom,
            receiverChatRoom = receiverChatRoom,
            onFirebaseCommunicationListener = onFirebaseCommunicationListener
        )
    }
}