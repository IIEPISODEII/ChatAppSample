package com.example.chatappsample.domain.usecase

import android.net.Uri
import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
import com.example.chatappsample.domain.`interface`.OnFirebaseCommunicationListener
import com.example.chatappsample.domain.`interface`.OnGetDataListener
import com.example.chatappsample.domain.dto.Message
import com.example.chatappsample.domain.repository.ChatRepository
import javax.inject.Inject

class DownloadFileUsecase @Inject constructor(private val repo: ChatRepository) {
    operator fun invoke(message: Message, onFileDownloadListener: OnFileDownloadListener) {
        repo.downloadFile(message, onFileDownloadListener)
    }
}