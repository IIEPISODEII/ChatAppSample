package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.repository.ChatroomRepository
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class FetchReaderLogFromRemoteDBUsecase @Inject constructor (private val repo: ChatroomRepository) {

    operator fun invoke(chatroomId: String, currentUserId: String, coroutineScope: CoroutineScope) {
        repo.fetchReaderLogFromRemoteDB(chatroomId, currentUserId, coroutineScope)
    }
}