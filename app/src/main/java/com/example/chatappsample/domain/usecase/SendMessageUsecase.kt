package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.`interface`.OnFirebaseCommunicationListener
import com.example.chatappsample.domain.dto.Message
import com.example.chatappsample.domain.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class SendMessageUsecase @Inject constructor(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(
        message: Message,
        senderChatRoom: String,
        receiverChatRoom: String,
        onFirebaseCommunicationListener: OnFirebaseCommunicationListener
    ) {
        chatRepository.sendMessage(
            message = message,
            myChatRoom = senderChatRoom,
            yourChatRoom = receiverChatRoom,
            onFirebaseCommunicationListener = onFirebaseCommunicationListener
        )
    }
}