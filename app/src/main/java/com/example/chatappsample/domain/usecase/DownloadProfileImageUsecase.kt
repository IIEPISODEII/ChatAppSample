package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.`interface`.FileDownloadListener
import com.example.chatappsample.domain.repository.UserRepository
import javax.inject.Inject

class DownloadProfileImageUsecase @Inject constructor(private val repo : UserRepository) {

    operator fun invoke(userID: String, fileDownloadListener: FileDownloadListener) {
        repo.downloadProfileImage(userID, fileDownloadListener)
    }
}