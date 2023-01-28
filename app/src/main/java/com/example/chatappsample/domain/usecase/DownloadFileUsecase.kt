package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.`interface`.FileDownloadListener
import com.example.chatappsample.domain.dto.MessageDomain
import com.example.chatappsample.domain.repository.ChatRepository
import javax.inject.Inject

class DownloadFileUsecase @Inject constructor(private val repo: ChatRepository) {
    operator fun invoke(messageDomain: MessageDomain, fileDownloadListener: FileDownloadListener) {
        repo.downloadFile(messageDomain, fileDownloadListener)
    }
}