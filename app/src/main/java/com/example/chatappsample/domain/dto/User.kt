package com.example.chatappsample.domain.dto

data class User(
    var name: String = "",
    var email: String = "",
    val uid: String = "",
    var profileImage: String = ""
)