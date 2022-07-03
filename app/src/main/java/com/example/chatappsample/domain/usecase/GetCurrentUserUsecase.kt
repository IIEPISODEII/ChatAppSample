package com.example.chatappsample.domain.usecase

import androidx.lifecycle.MutableLiveData
import com.example.chatappsample.domain.`interface`.OnGetDataListener
import com.example.chatappsample.domain.dto.User
import com.example.chatappsample.domain.repository.UserRepository
import javax.inject.Inject

class GetCurrentUserUsecase @Inject constructor (private val userRepository: UserRepository) {

    fun getCurrentUser(listener: OnGetDataListener) {
        userRepository.getCurrentUser(listener)
    }
}