package com.example.chatappsample.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.chatappsample.domain.dto.MessageDomain

@Entity(tableName="chatrooms", primaryKeys = ["currentAccountId", "chatRoomId"])
data class ChatRoomData(
    var currentAccountId: String = "",
    var chatRoomId: String = "",
    @Embedded
    var readerLog: ReaderLogData = ReaderLogData()
)