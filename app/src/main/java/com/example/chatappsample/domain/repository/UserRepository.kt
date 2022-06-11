package com.example.chatappsample.domain.repository

import com.example.chatappsample.domain.dto.User

interface UserRepository {
    fun getCurrentUser(event: () -> Unit): User?

    fun getAllUsers(event: () -> Unit): ArrayList<User>

    fun signOut(event: () -> Unit)
}