package com.example.chatappsample.domain.dto

data class MessageDomain(
    val messageId: String = "",
    val messageType: Int = 0,
    var message: String = "",
    val senderId: String = "",
    val sentTime: String = ""
) {
    companion object {
        const val TYPE_NORMAL_TEXT = 0
        const val TYPE_IMAGE = 1
    }
}