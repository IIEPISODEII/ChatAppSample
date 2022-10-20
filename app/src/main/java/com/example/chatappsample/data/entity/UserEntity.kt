package com.example.chatappsample.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.chatappsample.domain.dto.User

@Entity(tableName="users")
data class UserEntity(
    var userName: String,
    @PrimaryKey val userId: String,
    var userProfileImage: String,
    val userEmail: String
) {
    fun toUserDTO() = User(userName, userEmail, userId, userProfileImage)
}