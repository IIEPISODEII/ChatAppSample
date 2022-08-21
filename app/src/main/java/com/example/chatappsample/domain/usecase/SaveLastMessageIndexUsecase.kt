package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.repository.SharedPreferenceRepository
import javax.inject.Inject

class SaveLastMessageIndexUsecase @Inject constructor(private val repo: SharedPreferenceRepository) {
    operator fun invoke(chatRoom: String, index: Int) = repo.saveLastMessageIndex(chatRoom, index)
}