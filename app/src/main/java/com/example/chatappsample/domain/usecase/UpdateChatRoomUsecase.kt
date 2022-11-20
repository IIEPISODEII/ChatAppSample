package com.example.chatappsample.domain.usecase

import androidx.lifecycle.MutableLiveData
import com.example.chatappsample.domain.`interface`.OnGetDataListener
import com.example.chatappsample.domain.`interface`.OnGetRegistrationListener
import com.example.chatappsample.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class UpdateChatRoomUsecase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(myId: String, yourId: String, time: String, onSuccess: (String) -> Unit, onFail: () -> Unit, enter: Boolean, coroutineScope: CoroutineScope) {
        userRepository.updateChatRoomState(myId, yourId, time, onSuccess, onFail, enter, coroutineScope)
    }
}