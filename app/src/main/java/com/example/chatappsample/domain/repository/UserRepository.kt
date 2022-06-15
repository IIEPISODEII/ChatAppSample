package com.example.chatappsample.domain.repository

import androidx.lifecycle.MutableLiveData
import com.example.chatappsample.domain.dto.User

interface UserRepository {
    fun getCurrentUser(): User?

    fun getAllUsers(): ArrayList<User>

    fun signOut(): Boolean

    fun signUp(name: String, email: String, password: String): Boolean
}