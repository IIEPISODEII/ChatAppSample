package com.example.chatappsample.domain.repository

import com.example.chatappsample.domain.`interface`.OnGetDataListener
import com.example.chatappsample.domain.dto.Message

interface ChatRepository {
    fun getReceivedMessage(chatRoom: String, listener: OnGetDataListener)

    fun sendMessage(
        message: Message,
        senderChatRoom: String,
        receiverChatRoom: String
    ) : Boolean
}