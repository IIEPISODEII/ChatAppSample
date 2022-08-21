package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.repository.SharedPreferenceRepository
import javax.inject.Inject

class GetLastMessageIndexUsecase @Inject constructor(private val repo: SharedPreferenceRepository) {
    operator fun invoke(chatRoom: String) = repo.getLastMessageIndex(chatRoom)
}