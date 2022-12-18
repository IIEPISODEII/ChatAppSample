package com.example.chatappsample.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.chatappsample.domain.dto.MessageDomain

@Entity(tableName="readerlog", primaryKeys =["chatroomId", "userId"])
data class ReaderLogData(
    var chatroomId: String = "",
    var userId: String = "",
    var readTime: String = ""
)