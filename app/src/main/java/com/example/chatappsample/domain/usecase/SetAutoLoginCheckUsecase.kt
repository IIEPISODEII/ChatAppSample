package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.repository.SharedPreferenceRepository
import javax.inject.Inject

class SetAutoLoginCheckUsecase @Inject constructor(private val repo: SharedPreferenceRepository) {
    operator fun invoke(value: Boolean) { repo.setAutoLoginChecked(value) }
}