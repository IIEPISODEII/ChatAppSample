package com.example.chatappsample.domain.repository

import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
import com.example.chatappsample.domain.`interface`.OnFirebaseCommunicationListener
import com.example.chatappsample.domain.dto.ChatRoomDomain
import com.example.chatappsample.domain.dto.MessageDomain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    suspend fun fetchMessagesFromExternalDB(
        chatRoom: String,
        coroutineScope: CoroutineScope
    )

    suspend fun fetchMessagesFromRoomDBAsFlow(
        chatRoom: String,
        queriesSize: Int
    ): Flow<List<MessageDomain?>>

    suspend fun fetchMessagesFromRoomDB(
        chatRoom: String,
        queriesSize: Int,
        offset: Int
    ): List<MessageDomain?>

    suspend fun sendMessage(
        message: MessageDomain,
        chatRoom: String,
        onFirebaseCommunicationListener: OnFirebaseCommunicationListener
    )

    suspend fun uploadFile(
        message: MessageDomain,
        chatRoom: String,
        onFirebaseCommunicationListener: OnFirebaseCommunicationListener
    )

    suspend fun fetchLastMessageOfChatRoom(
        chatRoom: String
    ): Flow<MessageDomain?>

    fun downloadFile(
        message: MessageDomain,
        onFileDownloadListener: OnFileDownloadListener
    )

    suspend fun fetchChatRoomFromDB(
        chatRoomId: String
    ): Flow<List<ChatRoomDomain.ReaderLogDomain>>
}