package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.dto.User
import com.example.chatappsample.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllUsersFromRoomDBUsecase @Inject constructor(private val userRepository: UserRepository) {

    suspend operator fun invoke(): Flow<List<User>> {
        return userRepository.getAllUsersFromRoomDB()
    }

}