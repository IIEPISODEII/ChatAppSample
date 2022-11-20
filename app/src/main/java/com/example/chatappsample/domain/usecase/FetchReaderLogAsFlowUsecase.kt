package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.dto.ChatRoomDomain
import com.example.chatappsample.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FetchReaderLogAsFlowUsecase @Inject constructor(private val repo: ChatRepository) {
    suspend operator fun invoke(chatRoomId: String): Flow<List<ChatRoomDomain.ReaderLog>>
        = repo.fetchChatRoomFromDB(chatRoomId)
}