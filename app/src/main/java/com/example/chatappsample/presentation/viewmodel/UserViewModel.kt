package com.example.chatappsample.presentation.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
import com.example.chatappsample.domain.`interface`.OnGetDataListener
import com.example.chatappsample.domain.`interface`.OnGetRegistrationListener
import com.example.chatappsample.domain.dto.User
import com.example.chatappsample.domain.usecase.*
import com.example.chatappsample.util.Resource
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(
    private val getCurrentUserUsecase: GetCurrentUserUsecase,
    private val getAllUsersUsecase: GetAllUsersUsecase,
    private val signOutUsecase: SignOutUsecase,
    private val signUpUsecase: SignUpUsecase,
    private val setAutoLoginCheckUsecase: SetAutoLoginCheckUsecase,
    private val updateCurrentUserUsecase: UpdateCurrentUserUsercase,
    private val downloadProfileImageUsecase: DownloadProfileImageUsecase
) : ViewModel() {

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?>
        get() = _currentUser

    fun getCurrentUserInformation() {
        getCurrentUserUsecase.getCurrentUser(object : OnGetDataListener {
            override fun onSuccess(dataSnapshot: DataSnapshot) {
                val user =
                    dataSnapshot.getValue(User::class.java) ?: User()
                if (user.uid == "") return
                getAllUsers(currentUserId = user.uid)
                _currentUser.postValue(user)
            }

            override fun onStart() {}

            override fun <T> onFailure(error: T) {
                if (error is DatabaseError) Log.e("UserViewModel", error.message)
            }

        })
    }


    private val _allUsers = MutableLiveData<ArrayList<User>>()
    val allUsers: LiveData<ArrayList<User>>
        get() = _allUsers

    fun getAllUsers(currentUserId: String) {
        viewModelScope.launch {
            getAllUsersUsecase(object : OnGetDataListener {
                override fun onSuccess(dataSnapshot: DataSnapshot) {
                    val userList = arrayListOf<User>()
                    for (snapShot in dataSnapshot.children) {
                        val user = snapShot.getValue(User::class.java)

                        if ((user != null) && (user.uid != currentUserId)) {
                            userList.add(user)
                        }
                    }

                    _allUsers.postValue(userList)
                }

                override fun onStart() {}

                override fun <T> onFailure(error: T) {
                    if (error is DatabaseError) Log.e("UserViewModel", error.message)
                    else return
                }
            })
        }
    }


    private val _registrationStatus = MutableLiveData<Resource<AuthResult?>>()
    val registrationStatus: LiveData<Resource<AuthResult?>>
        get() = _registrationStatus

    private val _isSignedUp = MutableLiveData(false)
    val isSignedUp: LiveData<Boolean>
        get() = _isSignedUp

    fun signOut() {
        if (signOutUsecase.signOut()) _currentUser.value = null
    }

    fun signUp(name: String, email: String, password: String) {
        signUpUsecase.signUp(name, email, password, object : OnGetRegistrationListener {
            override fun onSuccess(task: Task<AuthResult>) {
                _isSignedUp.postValue(true)
                _registrationStatus.postValue(Resource.Success(task.result))
            }

            override fun onStart() {
                _registrationStatus.postValue(Resource.Loading(null))
            }

            override fun <T> onFailure(error: T) {
                if (error is Exception) Log.e("UserViewModel", error.message ?: "")
                else return
            }

        })
    }

    fun cancelAutoLogin() {
        setAutoLoginCheckUsecase(false)
    }

    fun updateCurrentUser(user: User, changeProfileImage: Boolean) {
        updateCurrentUserUsecase.invoke(user, changeProfileImage)
    }

    fun downloadProfileImage(user: User, onFileDownloadListener: OnFileDownloadListener) {
        downloadProfileImageUsecase.downloadProfileImage(user.uid, onFileDownloadListener)
    }

    private val _isProfileEditMode = MutableLiveData(false)
    val isProfileEditMode: LiveData<Boolean>
        get() = _isProfileEditMode

    fun toggleProfileEditMode(mode: Boolean) {
        _isProfileEditMode.postValue(mode)
    }
}