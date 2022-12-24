package com.example.chatappsample.data.entity

import androidx.room.Entity

@Entity(tableName="chatrooms", primaryKeys = ["currentAccountId", "chatroomId"])
data class ChatroomData(
    val currentAccountId: String = "",
    val chatroomId: String = "",
    val chatroomName: String = ""
)