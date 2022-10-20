package com.example.chatappsample.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.chatappsample.domain.dto.Message

@Entity(tableName="messages")
data class MessageEntity(
    @PrimaryKey val messageId: String,
    val chatRoom: String,
    val type: Int,
    val senderId: String,
    val message: String,
    val sentTime: String
) {
    fun toMessageDTO() = Message(
        messageId = messageId,
        messageType = type,
        message = message,
        senderId = senderId,
        sentTime = sentTime
    )
}