package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.dto.ChatroomDomain
import com.example.chatappsample.domain.repository.ChatroomRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class FetchChatroomListFromLocalDBAsFlowUsecase @Inject constructor(
    private val chatroomRepo: ChatroomRepository
) {

    suspend operator fun invoke(
        chatroomId: String
    ): Flow<List<ChatroomDomain.ReaderLogDomain>> {
        return chatroomRepo.fetchReaderLogFromLocalDBAsFlow(chatroomId)
    }
}