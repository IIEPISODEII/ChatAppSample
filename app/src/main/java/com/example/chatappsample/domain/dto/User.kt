package com.example.chatappsample.domain.dto

data class User(
    val name: String = "",
    var email: String = "",
    val uid: String = "",
    val profileImage: String = "",
    var lastMessage: Message = Message()
)