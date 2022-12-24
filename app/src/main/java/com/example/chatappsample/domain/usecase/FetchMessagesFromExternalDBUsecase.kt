package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class FetchMessagesFromExternalDBUsecase @Inject constructor(private val repo: ChatRepository) {

    suspend operator fun invoke(chatRoom: String, coroutineScope: CoroutineScope) {
        repo.fetchMessagesFromExternalDB(chatRoom, coroutineScope)
    }
}