package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.dto.User
import com.example.chatappsample.domain.repository.UserRepository
import javax.inject.Inject

class UpdateCurrentUserUsercase @Inject constructor(private val repo: UserRepository) {
    operator fun invoke(user: User, changeProfileImage: Boolean) {
        repo.updateCurrentUser(user, changeProfileImage)
    }
}