package com.example.chatappsample.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
import com.example.chatappsample.domain.`interface`.OnGetDataListener
import com.example.chatappsample.domain.`interface`.OnGetRegistrationListener
import com.example.chatappsample.domain.dto.MessageDomain
import com.example.chatappsample.domain.dto.UserDomain
import com.example.chatappsample.domain.usecase.*
import com.example.chatappsample.util.Resource
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(
    private val getCurrentUserUsecase: GetCurrentUserUsecase,
    private val receiveAllUsersFromExternalDBUsecase: ReceiveAllUsersFromExternalDBUsecase,
    private val getAllUsersUsecase: GetAllUsersFromRoomDBUsecase,
    private val signOutUsecase: SignOutUsecase,
    private val signUpUsecase: SignUpUsecase,
    private val setAutoLoginCheckUsecase: SetAutoLoginCheckUsecase,
    private val updateCurrentUserUsecase: UpdateCurrentUserUsercase,
    private val downloadProfileImageUsecase: DownloadProfileImageUsecase,
    private val takeLastMessageUsecase: FetchLastMessageUsecase,
    private val updateChatRoomUsecase: UpdateChatRoomUsecase
) : ViewModel() {

    private var currentUserId: String = ""
    fun setCurrentUserId(id: String) {
        currentUserId = id
        viewModelScope.launch {
            getAllUsers().collect {
                _allUsersList.postValue(it)
            }
        }
    }

    private val _currentUserDomain = MutableLiveData<UserDomain?>()
    val currentUserDomain: LiveData<UserDomain?>
        get() = _currentUserDomain

    fun getCurrentUserInformation() {
        getCurrentUserUsecase.getCurrentUser(object : OnGetDataListener {
            override fun onSuccess(dataSnapshot: DataSnapshot) {
                val userDomain =
                    dataSnapshot.getValue(UserDomain::class.java) ?: UserDomain()
                if (userDomain.uid == "") return
                _currentUserDomain.postValue(userDomain)
            }

            override fun onStart() {}

            override fun <T> onFailure(error: T) {
                if (error is DatabaseError) Log.e("UserViewModel", error.message)
            }
        })
    }

    private val _allUsersList = MutableLiveData<List<UserDomain>>()
    val allUsersList: LiveData<List<UserDomain>>
        get() = _allUsersList

    private suspend fun getAllUsers(): Flow<List<UserDomain>> {
        return getAllUsersUsecase().map { list -> list.filter { user -> user.uid != currentUserId } }
    }

    fun receiveAllUsersFromExternalDB() = receiveAllUsersFromExternalDBUsecase(viewModelScope)

    private val _registrationStatus = MutableLiveData<Resource<AuthResult?>>()
    val registrationStatus: LiveData<Resource<AuthResult?>>
        get() = _registrationStatus

    private val _isSignedUp = MutableLiveData(false)

    fun signOut() {
        if (signOutUsecase.signOut()) _currentUserDomain.value = null
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

    fun updateCurrentUser(userDomain: UserDomain, changeProfileImage: Boolean) {
        updateCurrentUserUsecase(userDomain, changeProfileImage)
    }

    fun downloadProfileImage(userDomain: UserDomain, onFileDownloadListener: OnFileDownloadListener) {
        downloadProfileImageUsecase(userDomain.uid, onFileDownloadListener)
    }

    private val _isProfileEditMode = MutableLiveData(false)
    val isProfileEditMode: LiveData<Boolean>
        get() = _isProfileEditMode

    fun toggleProfileEditMode(mode: Boolean) {
        _isProfileEditMode.postValue(mode)
    }

    suspend fun takeLastMessage(userDomain: UserDomain): Flow<MessageDomain?> {
        return try {
            takeLastMessageUsecase(currentUserDomain.value!!.uid + userDomain.uid)
        } catch (e: Exception) {
            e.printStackTrace()
            flow {
                emit(null)
            }
        }
    }

    fun updateChatRoom(myId: String, yourId: String, time: String, onSuccess: (String) -> Unit, onFail: () -> Unit, enter: Boolean) {
        updateChatRoomUsecase(myId, yourId, time, onSuccess, onFail, enter, viewModelScope)
    }
}