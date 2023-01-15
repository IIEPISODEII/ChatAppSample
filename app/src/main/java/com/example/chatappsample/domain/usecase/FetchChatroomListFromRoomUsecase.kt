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

class FetchChatroomListFromRoomUsecase @Inject constructor(
    private val chatroomRepo: ChatroomRepository,
    private val userRepo: UserRepository
) {

    suspend operator fun invoke(
        currentUserId: String
    ): Flow<List<ChatroomDomain>> {

        val chatroomDomainList = chatroomRepo.fetchChatroomListFromRoom(currentUserId)
            .catch {
                throw Exception("Fetching ChatroomList Failed.")
            }
            .map {
                it.map { chatroomDomain ->


                    chatroomDomain.apply {

                        this.readerLog = withContext(Dispatchers.IO) { chatroomRepo.fetchReaderLogFromRoom(chatroomDomain.chatroomId) }

                        this.chatroomName = this.readerLog.filter { readerLog ->
                            readerLog.userId != currentUserId
                        }.map { readerLogDomain ->
                            userRepo.fetchUserById(readerLogDomain.userId).name
                        }.joinToString(separator = ", ", postfix = "님과의 대화방")
                    }
                }
            }

        return chatroomDomainList
    }
}