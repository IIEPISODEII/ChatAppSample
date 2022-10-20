package com.example.chatappsample.domain.dto

import com.example.chatappsample.data.entity.MessageEntity

data class Message(
    val messageId: String = "",
    val messageType: Int = 0,
    var message: String = "",
    val senderId: String = "",
    val sentTime: String = ""
) {
    companion object {
        const val TYPE_NORMAL_TEXT = 0
        const val TYPE_IMAGE = 1
    }

    fun toMessageEntity(chatRoom: String) = MessageEntity(
        messageId = this.messageId,
        chatRoom = chatRoom,
        type = this.messageType,
        senderId = this.senderId,
        message = this.message,
        sentTime = this.sentTime
    )

    fun setMessageToFileUri(uri: String) {
        this.message = uri
    }
}