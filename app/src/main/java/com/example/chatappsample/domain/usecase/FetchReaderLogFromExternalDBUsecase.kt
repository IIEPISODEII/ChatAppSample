package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.repository.ChatroomRepository
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class FetchReaderLogFromExternalDBUsecase @Inject constructor (private val repo: ChatroomRepository) {

    operator fun invoke(chatroomId: String, currentUserId: String, coroutineScope: CoroutineScope) {
        repo.fetchReaderLogFromExternalDB(chatroomId, currentUserId, coroutineScope)
    }
}