package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.dto.ChatroomDomain
import com.example.chatappsample.domain.repository.ChatroomRepository
import com.example.chatappsample.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FetchChatroomListFromRoomAsFlowUsecase @Inject constructor(
    private val chatroomRepo: ChatroomRepository
) {

    suspend operator fun invoke(
        chatroomId: String
    ): Flow<List<ChatroomDomain.ReaderLogDomain>> {
        return chatroomRepo.fetchReaderLogFromRoomAsFlow(chatroomId)
    }
}