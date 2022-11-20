package com.example.chatappsample.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.chatappsample.domain.dto.MessageDomain

@Entity(tableName="messages")
data class MessageData(
    @PrimaryKey var messageId: String = "",
    var chatRoom: String = "",
    var type: Int = 0,
    var senderId: String = "",
    var message: String = "",
    var sentTime: String = ""
) {
    fun toDomain() = MessageDomain(
        messageId = messageId,
        messageType = type,
        message = message,
        senderId = senderId,
        sentTime = sentTime
    )
}