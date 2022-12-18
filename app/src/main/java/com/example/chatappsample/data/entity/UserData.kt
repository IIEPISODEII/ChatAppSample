package com.example.chatappsample.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.chatappsample.domain.dto.UserDomain

@Entity(tableName="users")
data class UserData(
    var name: String = "",
    @PrimaryKey val uid: String = "",
    var profileImage: String = "",
    var email: String = "",
    var lastTimeStamp: String = ""
) {
    fun toDomain() = UserDomain(name, email, uid, profileImage, lastTimeStamp)
}