package com.example.chatappsample.domain.repository

import android.net.Uri
import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
import com.example.chatappsample.domain.`interface`.OnFirebaseCommunicationListener
import com.example.chatappsample.domain.`interface`.OnGetDataListener
import com.example.chatappsample.domain.dto.Message
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
    ) : Flow<List<Message>>

    suspend fun fetchMessagesFromRoomDB(
        chatRoom: String,
        queriesSize: Int,
        offset: Int
    ) : List<Message>

    suspend fun sendMessage(
        message: Message,
        myChatRoom: String,
        yourChatRoom: String,
        onFirebaseCommunicationListener: OnFirebaseCommunicationListener
    )

    suspend fun uploadFile(
        message: Message,
        myChatRoom: String,
        yourChatRoom: String,
        onFirebaseCommunicationListener: OnFirebaseCommunicationListener
    )

    suspend fun takeLastMessageOfChatRoom(
        chatRoom: String
    ): Flow<Message>

    fun downloadFile(
        message: Message,
        onFileDownloadListener: OnFileDownloadListener
    )
}