package com.example.chatappsample.domain.repository

import com.example.chatappsample.domain.dto.Message

interface ChatRepository {
    fun getReceivedMessage(chatRoom: String, event: () -> Unit): ArrayList<Message>

    fun sendMessage(
        message: Message,
        senderChatRoom: String,
        receiverChatRoom: String,
        sendListener: () -> Unit
    )
}