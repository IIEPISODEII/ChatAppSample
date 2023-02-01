package com.example.chatappsample.domain.repository

import com.example.chatappsample.domain.`interface`.FileDownloadListener
import com.example.chatappsample.domain.`interface`.FileUploadListener
import com.example.chatappsample.domain.dto.MessageDomain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    fun fetchMessagesFromRemoteDB(
        chatRoom: String,
        coroutineScope: CoroutineScope
    )

    suspend fun fetchMessagesFromLocalDBAsFlow(
        chatRoom: String,
        queriesSize: Int
    ): Flow<List<MessageDomain?>>

    suspend fun fetchMessagesFromLocalDB(
        chatRoom: String,
        queriesSize: Int,
        offset: Int
    ): List<MessageDomain?>

    fun sendMessage(
        message: MessageDomain,
        chatRoom: String,
        fileUploadListener: FileUploadListener
    )

    suspend fun uploadFile(
        message: MessageDomain,
        chatRoom: String,
        fileUploadListener: FileUploadListener
    )

    suspend fun fetchLastMessageOfChatRoom(
        chatRoom: String
    ): Flow<MessageDomain?>

    fun downloadFile(
        message: MessageDomain,
        fileDownloadListener: FileDownloadListener
    )
}