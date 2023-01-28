package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.dto.ChatroomDomain
import com.example.chatappsample.domain.repository.ChatroomRepository
import com.example.chatappsample.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FetchChatroomListFromLocalDBUsecase @Inject constructor(
    private val chatroomRepo: ChatroomRepository,
    private val userRepo: UserRepository
) {

    suspend operator fun invoke(
        currentUserId: String
    ): Flow<List<ChatroomDomain>> {

        val chatroomDomainList = chatroomRepo.fetchChatroomListFromLocalDB(currentUserId)
            .catch {
                throw Exception("Fetching ChatroomList Failed.")
            }
            .map {
                it.map { chatroomDomain ->
                    chatroomDomain.apply {
                        while(this.readerLog.isEmpty()) {
                            this.readerLog = withContext(Dispatchers.IO) { chatroomRepo.fetchReaderLogFromLocalDB(chatroomDomain.chatroomId) }
                        }

                        this.chatroomName = this.readerLog.filter { readerLog ->
                            readerLog.userId != currentUserId
                        }.map { readerLogDomain ->
                            userRepo.fetchUserById(readerLogDomain.userId).name
                        }.joinToString(separator = ", ", postfix = "님과의 대화방")
                    }
                    chatroomDomain
                }
            }

        return chatroomDomainList
    }
}