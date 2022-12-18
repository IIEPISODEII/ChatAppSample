package com.example.chatappsample.domain.dto

data class ChatRoomDomain(
    val chatRoomId: String = "",
    val participantsLog: List<ReaderLogDomain> = listOf()
) {
    data class ReaderLogDomain(val userId: String = "", val readTime: String = "")
}