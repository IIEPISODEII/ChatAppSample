package com.example.chatappsample.domain.usecase

import androidx.lifecycle.MutableLiveData
import com.example.chatappsample.domain.dto.User
import com.example.chatappsample.domain.repository.UserRepository
import javax.inject.Inject

class GetAllUsersUsecase @Inject constructor(private val userRepository: UserRepository) {

    fun getAllUsers(): ArrayList<User> {
        return userRepository.getAllUsers()
    }

}