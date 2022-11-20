package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.`interface`.OnFirebaseCommunicationListener
import com.example.chatappsample.domain.dto.MessageDomain
import com.example.chatappsample.domain.repository.ChatRepository
import javax.inject.Inject

class UploadFileUsecase @Inject constructor(private val repo: ChatRepository) {

    suspend operator fun invoke(
        messageDomain: MessageDomain,
        chatRoom: String,
        onFirebaseCommunicationListener: OnFirebaseCommunicationListener
    ) {
        repo.uploadFile(
            messageDomain,
            chatRoom,
            onFirebaseCommunicationListener
        )
    }
}
