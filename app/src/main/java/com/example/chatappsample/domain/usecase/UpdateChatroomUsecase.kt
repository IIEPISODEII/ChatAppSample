package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.repository.ChatroomRepository
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class UpdateChatroomUsecase @Inject constructor(
    private val repo: ChatroomRepository
) {
    operator fun invoke(myId: String, yourId: String, time: String, onSuccess: (String) -> Unit, onFail: () -> Unit, enter: Boolean) {
        repo.updateChatroomState(myId, yourId, time, onSuccess, onFail, enter)
    }
}