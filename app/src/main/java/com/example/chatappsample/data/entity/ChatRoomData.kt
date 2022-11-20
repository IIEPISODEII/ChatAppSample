package com.example.chatappsample.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.chatappsample.domain.dto.MessageDomain

@Entity(tableName="chatrooms", primaryKeys = ["chatRoomId", "participantsId"])
data class ChatRoomData(
    var chatRoomId: String = "",
    var participantsId: String = "",
    var participationTime: String = ""
)