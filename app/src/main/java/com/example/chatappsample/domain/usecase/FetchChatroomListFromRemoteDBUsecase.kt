package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.repository.ChatroomRepository
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class FetchChatroomListFromRemoteDBUsecase @Inject constructor (private val repo: ChatroomRepository) {

    operator fun invoke(currentUserId: String, coroutineScope: CoroutineScope) {
        repo.fetchChatroomListFromRemoteDB(currentUserId, coroutineScope)
    }
}