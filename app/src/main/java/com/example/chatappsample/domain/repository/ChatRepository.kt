package com.example.chatappsample.domain.repository

import android.net.Uri
import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
import com.example.chatappsample.domain.`interface`.OnFirebaseCommunicationListener
import com.example.chatappsample.domain.`interface`.OnGetDataListener
import com.example.chatappsample.domain.dto.Message

interface ChatRepository {

    fun receiveAllMessages(
        chatRoom: String,
        listener: OnGetDataListener
    )

    fun receiveAdditionalMessage(
        chatRoom: String,
        queriesSize: Int,
        listener: OnGetDataListener
    )

    fun sendMessage(
        message: Message,
        senderChatRoom: String,
        receiverChatRoom: String,
        onFirebaseCommunicationListener: OnFirebaseCommunicationListener
    ) : Boolean

    fun uploadFile(
        message: Message,
        senderChatRoom: String,
        receiverChatRoom: String,
        onFirebaseCommunicationListener: OnFirebaseCommunicationListener
    )

    fun downloadFile(
        uri: Uri,
        onFileDownloadListener: OnFileDownloadListener
    )
}