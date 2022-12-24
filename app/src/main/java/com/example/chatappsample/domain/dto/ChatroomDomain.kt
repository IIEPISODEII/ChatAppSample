package com.example.chatappsample.domain.dto

data class ChatroomDomain(
    var chatroomId: String = "",
    var chatroomName: String = "",
    var readerLog: List<ReaderLogDomain> = listOf()
) {
    data class ReaderLogDomain(var userId: String = "", var readTime: String = "")
}