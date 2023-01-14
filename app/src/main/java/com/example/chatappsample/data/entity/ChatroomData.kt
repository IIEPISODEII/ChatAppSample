package com.example.chatappsample.data.entity

import androidx.room.Entity

@Entity(tableName = "chatrooms", primaryKeys = ["currentUserId", "chatroomId"])
data class ChatroomData(
    val currentUserId: String = "",
    val chatroomId: String = "",
    val chatroomName: String = ""
)