package com.example.chatappsample.domain.dto

data class Message(
    var messageIndex: Int = 0,
    val message: String = "",
    var imageUri: String = "",
    val senderId: String = "",
    val sentTime: String = ""
)