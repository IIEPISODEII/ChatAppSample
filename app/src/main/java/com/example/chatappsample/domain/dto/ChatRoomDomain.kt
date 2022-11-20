package com.example.chatappsample.domain.dto

data class ChatRoomDomain(
    val chatRoomId: String = "",
    val participantsLog: List<ReaderLog> = listOf()
) {
    data class ReaderLog(val id: String, val time: String)
}