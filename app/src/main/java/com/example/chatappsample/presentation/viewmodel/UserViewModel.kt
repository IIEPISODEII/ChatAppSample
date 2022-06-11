package com.example.chatappsample.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.chatappsample.domain.dto.User
import com.example.chatappsample.domain.usecase.GetAllUsersUsecase
import com.example.chatappsample.domain.usecase.GetCurrentUserUsecase
import com.example.chatappsample.domain.usecase.SignoutUsecase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(
    private val getCurrentUserUsecase: GetCurrentUserUsecase,
    private val getAllUsersUsecase: GetAllUsersUsecase,
    private val signoutUsecase: SignoutUsecase
    ): ViewModel() {

    fun getCurrentUser(event: () -> Unit): User? {
        return getCurrentUserUsecase.getCurrentUser {
            event()
        }
    }

    fun getAllUsers(event: () -> Unit): ArrayList<User> {
        return getAllUsersUsecase.getAllUsers(event)
    }

    fun signOut(event: () -> Unit) {
        return signoutUsecase.signOut { event() }
    }
}