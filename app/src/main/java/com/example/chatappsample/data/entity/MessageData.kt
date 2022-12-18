package com.example.chatappsample.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.chatappsample.domain.dto.MessageDomain

@Entity(
    tableName = "messages",
    primaryKeys = ["messageId"]
)
data class MessageData(
    var messageId: String = "",
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