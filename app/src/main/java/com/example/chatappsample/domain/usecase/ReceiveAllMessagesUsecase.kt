package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.`interface`.OnGetDataListener
import com.example.chatappsample.domain.repository.ChatRepository
import javax.inject.Inject

class ReceiveAllMessagesUsecase @Inject constructor(private val repo: ChatRepository) {

    operator fun invoke(chatRoom: String, listener: OnGetDataListener) {
        repo.receiveAllMessages(chatRoom, listener)
    }
}