package com.example.chatappsample.domain.usecase

import androidx.lifecycle.MutableLiveData
import com.example.chatappsample.domain.`interface`.OnGetDataListener
import com.example.chatappsample.domain.dto.User
import com.example.chatappsample.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetAllUsersUsecase @Inject constructor(private val userRepository: UserRepository) {

    fun getAllUsers(listener: OnGetDataListener) {
        userRepository.getAllUsers(listener)
    }

}