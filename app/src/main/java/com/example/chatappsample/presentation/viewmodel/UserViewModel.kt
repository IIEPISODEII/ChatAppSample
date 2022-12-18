package com.example.chatappsample.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatappsample.domain.`interface`.*
import com.example.chatappsample.domain.dto.ChatRoomDomain
import com.example.chatappsample.domain.dto.UserDomain
import com.example.chatappsample.domain.usecase.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(
    private val fetchCurrentUserUsecase: FetchCurrentUserUsecase,
    private val fetchUserListFromExternalDBUsecase: FetchUserListFromExternalDBUsecase,
    private val fetchUserListUsecase: FetchUserListFromRoomDBUsecase,
    private val fetchChatRoomListUsecase: FetchChatRoomListUsecase,
    private val signUpUsecase: SignUpUsecase,
    private val signInUsecase: SignInUsecase,
    private val signOutUsecase: SignOutUsecase,
    private val sendEmailVerificationUsecase: SendEmailVerificationUsecase,
    private val setAutoLoginCheckUsecase: SetAutoLoginCheckUsecase,
    private val getAutoLoginCheckUsecase: GetAutoLoginCheckUsecase,
    private val updateCurrentUserUsecase: UpdateCurrentUserUsercase,
    private val downloadProfileImageUsecase: DownloadProfileImageUsecase,
    private val fetchLastMessageUsecase: FetchLastMessageUsecase,
    private val updateChatRoomUsecase: UpdateChatRoomUsecase
) : ViewModel() {

    private var currentUserId: String = ""
    private val _chatRoomList = MutableLiveData<List<ChatRoomDomain>>()
    val chatRoomList: LiveData<List<ChatRoomDomain>>
        get() = _chatRoomList
    fun setCurrentUserIdAndFetchChatroomList(id: String) {
        currentUserId = id
        viewModelScope.launch {
            fetchChatRoomListUsecase(currentUserId).collect {
                _chatRoomList.postValue(it)
            }
        }
    }

    fun fetchAllUsersList() {
        viewModelScope.launch {
            fetchAllUsers().collect {
                _userList.postValue(it)
            }
        }
    }

    private var currentUserEmail = ""
    private var currentUserPassword = ""

    fun getCurrentEmail() = currentUserEmail
    fun getCurrentPassword() = currentUserPassword

    private val _currentUserDomain = MutableLiveData<UserDomain?>()
    val currentUserDomain: LiveData<UserDomain?>
        get() = _currentUserDomain

    fun getCurrentUserInformation() {
        fetchCurrentUserUsecase(object : OnGetDataListener {
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

    private val _userList = MutableLiveData<List<UserDomain>>()
    val userList: LiveData<List<UserDomain>>
        get() = _userList

    private suspend fun fetchAllUsers(): Flow<List<UserDomain>> {
        return fetchUserListUsecase().map { list -> list.filter { user -> user.uid != currentUserId } }
    }

    fun fetchUserListFromExternalDB() = fetchUserListFromExternalDBUsecase(viewModelScope)

    fun signIn(email: String, password: String, listener: OnSignInListener) {
        signInUsecase(email, password, listener)
    }

    fun signOut() {
        if (signOutUsecase.signOut()) _currentUserDomain.value = null
    }

    fun sendVerificationEmail(email: String, password: String, listener: OnSendEmailVerificationListener) {
        sendEmailVerificationUsecase(email, password, listener)
        currentUserEmail = email
        currentUserPassword = password
    }

    fun signUp(name: String, listener: OnEmailVerificationListener) {
        signUpUsecase(name, listener)
    }

    fun cancelAutoLogin() {
        setAutoLoginCheckUsecase(false)
    }

    fun getAutoLoginCheck(): Boolean {
        return getAutoLoginCheckUsecase()
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

//    suspend fun takeLastMessage(): Flow<MessageDomain?> {
//        viewModelScope.launch {
//
//            getChatRoomIdsByParticipantsId(currentUserId, allUsersList.value?.map { it.uid } ?: listOf())
//
//        }
//
//        return try {
//            takeLastMessageUsecase(currentUserDomain.value!!.uid + userDomain.uid)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            flow {
//                emit(null)
//            }
//        }
//    }

    fun updateChatRoom(myId: String, yourId: String, time: String, onSuccess: (String) -> Unit, onFail: () -> Unit, enter: Boolean) {
        updateChatRoomUsecase(myId, yourId, time, onSuccess, onFail, enter, viewModelScope)
    }

    companion object {
        const val SEND_EMAIL_VERIFICATION_SUCCESS = 0
        const val SEND_EMAIL_VERIFICATION_FAIL = 1
        const val SEND_EMAIL_VERIFICATION_ON_PROGRESS = 2
    }
}