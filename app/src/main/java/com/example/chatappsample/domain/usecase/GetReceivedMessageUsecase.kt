package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.`interface`.OnGetDataListener
import com.example.chatappsample.domain.repository.ChatRepository
import javax.inject.Inject

class GetReceivedMessageUsecase @Inject constructor(private val chatRepository: ChatRepository) {

    operator fun invoke(chatRoom: String, queriesSize: Int, listener: OnGetDataListener) {
        chatRepository.receiveAdditionalMessage(chatRoom, queriesSize, listener)
    }
}