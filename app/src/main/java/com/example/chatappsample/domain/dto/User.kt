package com.example.chatappsample.domain.dto

import com.example.chatappsample.data.entity.UserEntity

data class User(
    var name: String = "",
    var email: String = "",
    val uid: String = "",
    var profileImage: String = ""
) {
    fun toUserEntity(): UserEntity = UserEntity(name, uid, profileImage, email)
}