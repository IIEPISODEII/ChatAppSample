package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.`interface`.OnGetDataListener
import com.example.chatappsample.domain.dto.Message
import com.example.chatappsample.domain.repository.ChatRepository
import javax.inject.Inject

class GetReceivedMessageUsecase @Inject constructor(private val chatRepository: ChatRepository) {

    fun getReceivedMessage(chatRoom: String, listener: OnGetDataListener) {
        return chatRepository.getReceivedMessage(chatRoom, listener)
    }
}