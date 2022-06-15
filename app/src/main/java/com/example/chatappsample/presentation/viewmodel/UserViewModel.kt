package com.example.chatappsample.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatappsample.domain.dto.User
import com.example.chatappsample.domain.usecase.GetAllUsersUsecase
import com.example.chatappsample.domain.usecase.GetCurrentUserUsecase
import com.example.chatappsample.domain.usecase.SignOutUsecase
import com.example.chatappsample.domain.usecase.SignUpUsecase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(
    private val getCurrentUserUsecase: GetCurrentUserUsecase,
    private val getAllUsersUsecase: GetAllUsersUsecase,
    private val signOutUsecase: SignOutUsecase,
    private val signUpUsecase: SignUpUsecase
    ): ViewModel() {

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?>
        get() = _currentUser

    private val _allUsers = MutableLiveData<ArrayList<User>>()
    val allUsers: LiveData<ArrayList<User>>
        get() = _allUsers

    private val _isSignedUp = MutableLiveData(false)
    val isSignedUp: LiveData<Boolean>
        get() = _isSignedUp


    fun getCurrentUser() {
        _currentUser.value = getCurrentUserUsecase.getCurrentUser()
    }

    fun getAllUsers() {
        _allUsers.value = getAllUsersUsecase.getAllUsers()
    }

    fun signOut() {
        if (signOutUsecase.signOut()) _currentUser.value = null
    }

    fun signUp(name: String, email: String, password: String) {
        signUpUsecase.signUp(name, email, password)

        _isSignedUp.value = true
    }
}