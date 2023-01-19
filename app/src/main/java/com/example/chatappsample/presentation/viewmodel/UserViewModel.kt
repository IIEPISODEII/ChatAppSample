package com.example.chatappsample.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatappsample.domain.`interface`.OnEmailVerificationListener
import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
import com.example.chatappsample.domain.`interface`.OnSendEmailVerificationListener
import com.example.chatappsample.domain.`interface`.OnSignInListener
import com.example.chatappsample.domain.dto.ChatroomDomain
import com.example.chatappsample.domain.dto.MessageDomain
import com.example.chatappsample.domain.dto.UserDomain
import com.example.chatappsample.domain.usecase.*
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
    private val fetchMessagesFromExternalDBUsecase: FetchMessagesFromExternalDBUsecase,
    private val fetchMessageFromRoomDBUsecase: FetchMessagesFromRoomDBUsecase,
    private val fetchLastMessageUsecase: FetchLastMessageUsecase,
    private val updateChatRoomUsecase: UpdateChatroomUsecase
) : ViewModel() {

    private var currentUserId: String = ""
    fun currentUser() = currentUserId

    private val _chatRoomList = MutableStateFlow<List<ChatroomDomain>>(listOf())
    val chatroomList: StateFlow<List<ChatroomDomain>>
        get() = _chatRoomList

    suspend fun setCurrentUserAndFetchChatroomList(id: String, coroutineScope: CoroutineScope) {
        if (currentUserId != "") return

        currentUserId = id
        fetchChatRoomListFromExternalDBUsecase(currentUserId, coroutineScope)
        _chatRoomList.value =
            fetchChatRoomListFromRoomUsecase(currentUserId).stateIn(coroutineScope).value
        fetchCurrentUserFromRoom(currentUserId)
    }

    fun fetchAllUsersList(myId: String) {
        viewModelScope.launch {
            fetchAllUsers(myId).collect {
                _userList.postValue(it)
            }
        }
    }

    private val _currentUserDomain = MutableLiveData<UserDomain>()
    val currentUserDomain: LiveData<UserDomain>
        get() = _currentUserDomain

    private val _userList = MutableLiveData<List<UserDomain>>()
    val userList: LiveData<List<UserDomain>>
        get() = _userList

    private suspend fun fetchAllUsers(myId: String): Flow<List<UserDomain>> {
        return fetchUserListUsecase(myId).stateIn(viewModelScope)
    }

    private fun fetchCurrentUserFromRoom(uid: String) {
        viewModelScope.launch {
            fetchCurrentUserUsecase(uid).collect {
                if (it.uid.isEmpty()) return@collect

                _currentUserDomain.value = it
            }
        }
    }

    fun fetchUserListFromExternalDB() = fetchUserListFromExternalDBUsecase(viewModelScope)

    fun signIn(email: String, password: String, listener: OnSignInListener) {
        signInUsecase(email, password, listener)
    }

    fun signOut() {
        if (signOutUsecase.signOut()) _currentUserDomain.value = UserDomain()
    }

    fun sendVerificationEmail(
        email: String,
        password: String,
        listener: OnSendEmailVerificationListener
    ) {
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

    fun downloadProfileImage(userId: String, onFileDownloadListener: OnFileDownloadListener) {
        downloadProfileImageUsecase(userId, onFileDownloadListener)
    }

    private val _isProfileEditMode = MutableLiveData(false)
    val isProfileEditMode: LiveData<Boolean>
        get() = _isProfileEditMode

    fun toggleProfileEditMode(mode: Boolean) {
        _isProfileEditMode.postValue(mode)
    }

    suspend fun fetchLastMessage(chatRoom: ChatroomDomain): Flow<MessageDomain?> {
        return fetchLastMessageUsecase(chatRoom.chatroomId).stateIn(viewModelScope)
    }

    fun updateChatRoom(
        myId: String,
        yourId: String,
        time: String,
        onSuccess: (String) -> Unit,
        onFail: () -> Unit,
        enter: Boolean
    ) {
        updateChatRoomUsecase(myId, yourId, time, onSuccess, onFail, enter)
    }

    fun fetchMessagesFromExternalDB(chatroom: String, coroutineScope: CoroutineScope) {
        fetchMessagesFromExternalDBUsecase(chatroom, coroutineScope)
    }
}