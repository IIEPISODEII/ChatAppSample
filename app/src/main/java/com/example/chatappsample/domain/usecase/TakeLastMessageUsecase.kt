package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.`interface`.OnGetDataListener
import com.example.chatappsample.domain.repository.ChatRepository
import javax.inject.Inject

class TakeLastMessageUsecase @Inject constructor(private val repo: ChatRepository) {
    operator fun invoke(chatRoom: String, listener: OnGetDataListener) = repo.takeLastMessageOfChatRoom(chatRoom, listener)
}