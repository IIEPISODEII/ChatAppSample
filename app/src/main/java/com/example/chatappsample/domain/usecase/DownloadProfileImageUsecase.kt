package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
import com.example.chatappsample.domain.repository.UserRepository
import javax.inject.Inject

class DownloadProfileImageUsecase @Inject constructor(private val repo : UserRepository) {

    operator fun invoke(userID: String, onFileDownloadListener: OnFileDownloadListener) {
        repo.downloadProfileImage(userID, onFileDownloadListener)
    }
}