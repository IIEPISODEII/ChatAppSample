package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.dto.ChatroomDomain
import com.example.chatappsample.domain.repository.ChatroomRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FetchChatroomListFromExternalDBUsecase @Inject constructor (private val repo: ChatroomRepository) {

    operator fun invoke(currentUserId: String, coroutineScope: CoroutineScope) {
        repo.fetchChatroomListFromExternalDB(currentUserId, coroutineScope)
    }
}