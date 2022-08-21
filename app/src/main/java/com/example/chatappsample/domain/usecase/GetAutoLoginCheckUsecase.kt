package com.example.chatappsample.domain.usecase

import com.example.chatappsample.domain.repository.SharedPreferenceRepository
import javax.inject.Inject

class GetAutoLoginCheckUsecase @Inject constructor(private val repo: SharedPreferenceRepository) {
    operator fun invoke() : Boolean { return repo.isAutoLoginChecked() }
}