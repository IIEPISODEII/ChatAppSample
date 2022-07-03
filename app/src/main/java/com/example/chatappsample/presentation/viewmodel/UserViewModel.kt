package com.example.chatappsample.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatappsample.domain.`interface`.OnGetDataListener
import com.example.chatappsample.domain.dto.User
import com.example.chatappsample.domain.repository.UserRepository
import com.example.chatappsample.domain.usecase.GetAllUsersUsecase
import com.example.chatappsample.domain.usecase.GetCurrentUserUsecase
import com.example.chatappsample.domain.usecase.SignOutUsecase
import com.example.chatappsample.domain.usecase.SignUpUsecase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
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
        getCurrentUserUsecase.getCurrentUser(object: OnGetDataListener {
            override fun onSuccess(dataSnapshot: DataSnapshot) {
                val currentUser = dataSnapshot.getValue(User::class.java) ?: throw DatabaseException("NO USER")

                _currentUser.postValue(currentUser)
            }

            override fun onStart() {

            }

            override fun <T> onFailure(error: T) {
                if (error is DatabaseError) Log.e("UserViewModel", error.message)
            }

        })
    }

    fun getAllUsers() {
        viewModelScope.launch {
            getAllUsersUsecase.getAllUsers(object: OnGetDataListener {
                override fun onSuccess(dataSnapshot: DataSnapshot) {
                    val userList = arrayListOf<User>()
                    for (snapShot in dataSnapshot.children) {
                        val user = snapShot.getValue(User::class.java)

                        if ((user != null) && (user.uid != currentUser.value?.uid)) {
                            userList.add(user)
                        }
                    }

                    _allUsers.postValue(userList)
                }

                override fun onStart() {

                }

                override fun <T> onFailure(error: T) {
                    if (error is DatabaseError) Log.e("UserViewModel", error.message)
                    else return
                }
            })
        }
    }

    fun signOut() {
        if (signOutUsecase.signOut()) _currentUser.value = null
    }

    fun signUp(name: String, email: String, password: String) {
        signUpUsecase.signUp(name, email, password)

        _isSignedUp.value = true
    }
}