package com.example.chatappsample.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatappsample.domain.`interface`.*
import com.example.chatappsample.domain.dto.ChatroomDomain
import com.example.chatappsample.domain.dto.MessageDomain
import com.example.chatappsample.domain.dto.UserDomain
import com.example.chatappsample.domain.usecase.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(
    private val fetchCurrentUserUsecase: FetchCurrentUserUsecase,
    private val fetchUserListFromExternalDBUsecase: FetchUserListFromExternalDBUsecase,
    private val fetchUserListUsecase: FetchUserListFromRoomDBUsecase,
    private val fetchChatRoomListFromExternalDBUsecase: FetchChatroomListFromExternalDBUsecase,
    private val fetchChatRoomListFromRoomUsecase: FetchChatroomListFromRoomUsecase,
    private val signUpUsecase: SignUpUsecase,
    private val signInUsecase: SignInUsecase,
    private val signOutUsecase: SignOutUsecase,
    private val sendEmailVerificationUsecase: SendEmailVerificationUsecase,
    private val setAutoLoginCheckUsecase: SetAutoLoginCheckUsecase,
    private val getAutoLoginCheckUsecase: GetAutoLoginCheckUsecase,
    private val updateCurrentUserUsecase: UpdateCurrentUserUsercase,
    private val downloadProfileImageUsecase: DownloadProfileImageUsecase,
    private val fetchLastMessageUsecase: FetchLastMessageUsecase,
    private val updateChatRoomUsecase: UpdateChatroomUsecase
) : ViewModel() {

    private var currentUserId: String = ""
    private var _chatRoomList = MutableStateFlow<List<ChatroomDomain>>(listOf())
    val chatroomList: StateFlow<List<ChatroomDomain>>
        get() = _chatRoomList
    suspend fun setCurrentUserIdAndFetchChatroomList(id: String, coroutineScope: CoroutineScope) {
        currentUserId = id
        fetchChatRoomListFromExternalDBUsecase(currentUserId, coroutineScope)
        _chatRoomList.value = fetchChatRoomListFromRoomUsecase(currentUserId).stateIn(coroutineScope).value
    }

    fun fetchAllUsersList() {
        viewModelScope.launch {
            fetchAllUsers().collect {
                _userList.postValue(it)
            }
        }
    }

    private val _currentUserDomain = MutableLiveData<UserDomain?>()
    val currentUserDomain: LiveData<UserDomain?>
        get() = _currentUserDomain

    fun fetchCurrentUserInformation() {
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

    suspend fun fetchLastMessage(chatRoom: ChatroomDomain): StateFlow<MessageDomain?> {
        return fetchLastMessageUsecase(chatRoom.chatroomId).stateIn(viewModelScope)
    }

    fun updateChatRoom(myId: String, yourId: String, time: String, onSuccess: (String) -> Unit, onFail: () -> Unit, enter: Boolean) {
        updateChatRoomUsecase(myId, yourId, time, onSuccess, onFail, enter, viewModelScope)
    }
}