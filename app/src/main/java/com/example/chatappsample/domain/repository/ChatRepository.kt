package com.example.chatappsample.domain.repository

import com.example.chatappsample.domain.dto.Message

interface ChatRepository {
    fun getReceivedMessage(chatRoom: String): ArrayList<Message>

    fun sendMessage(
        message: Message,
        senderChatRoom: String,
        receiverChatRoom: String
    ) : Boolean
}