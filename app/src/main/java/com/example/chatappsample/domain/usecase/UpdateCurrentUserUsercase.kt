package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.dto.UserDomain
import com.example.chatappsample.domain.repository.UserRepository
import javax.inject.Inject

class UpdateCurrentUserUsercase @Inject constructor(private val repo: UserRepository) {
    operator fun invoke(userDomain: UserDomain, changeProfileImage: Boolean) {
        repo.updateCurrentUser(userDomain, changeProfileImage)
    }
}