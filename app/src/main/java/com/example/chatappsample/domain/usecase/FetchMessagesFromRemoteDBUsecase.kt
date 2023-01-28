package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class FetchMessagesFromRemoteDBUsecase @Inject constructor(private val repo: ChatRepository) {

    operator fun invoke(chatRoom: String, coroutineScope: CoroutineScope) {
        repo.fetchMessagesFromRemoteDB(chatRoom, coroutineScope)
    }
}