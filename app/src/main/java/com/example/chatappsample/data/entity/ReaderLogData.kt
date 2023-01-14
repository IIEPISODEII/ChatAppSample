package com.example.chatappsample.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE

@Entity(
    tableName = "readerlog",
    primaryKeys = ["chatroomId", "userId"],
    foreignKeys = [ForeignKey(
        entity = ChatroomData::class,
        parentColumns = ["chatroomId", "currentUserId"],
        childColumns = ["chatroomId", "currentAccountId"],
        onDelete = CASCADE
    )]
)
data class ReaderLogData(
    var chatroomId: String = "",
    var currentAccountId: String = "",
    var userId: String = "",
    var readTime: String = ""
)