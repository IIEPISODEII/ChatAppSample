package com.example.chatappsample.presentation.viewmodel

import androidx.lifecycle.*
import com.example.chatappsample.domain.`interface`.EmailVerifyListener
import com.example.chatappsample.domain.`interface`.FileDownloadListener
import com.example.chatappsample.domain.`interface`.EmailVerificationSendListener
import com.example.chatappsample.domain.`interface`.SignInListener
import com.example.chatappsample.domain.dto.ChatroomDomain
import com.example.chatappsample.domain.dto.MessageDomain
import com.example.chatappsample.domain.dto.UserDomain
import com.example.chatappsample.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(
    private val fetchCurrentUserUsecase: FetchCurrentUserUsecase,
    private val fetchUserListFromRemoteDBUsecase: FetchUserListFromRemoteDBUsecase,
    private val fetchUserListUsecase: FetchUserListFromLocalDBUsecase,
    private val fetchChatRoomListFromRemoteDBUsecase: FetchChatroomListFromRemoteDBUsecase,
    private val fetchChatRoomListUsecase: FetchChatroomListFromLocalDBUsecase,
    private val signUpUsecase: SignUpUsecase,
    private val signInUsecase: SignInUsecase,
    private val sendEmailVerificationUsecase: SendEmailVerificationUsecase,
    private val setAutoLoginCheckUsecase: SetAutoLoginCheckUsecase,
    private val getAutoLoginCheckUsecase: GetAutoLoginCheckUsecase,
    private val updateCurrentUserUsecase: UpdateCurrentUserUsercase,
    private val downloadProfileImageUsecase: DownloadProfileImageUsecase,
    private val fetchMessagesFromRemoteDBUsecase: FetchMessagesFromRemoteDBUsecase,
    private val fetchLastMessageUsecase: FetchLastMessageUsecase,
    private val updateChatRoomUsecase: UpdateChatroomUsecase
) : ViewModel() {

    val currentUserInfo = fetchCurrentUserUsecase(currentUserId()).asLiveData()

    fun fetchChatroomListFromRemoteDB() {
        fetchChatRoomListFromRemoteDBUsecase(currentUserId(), viewModelScope)
    }

    fun fetchUserListFromRemoteDB() = fetchUserListFromRemoteDBUsecase(viewModelScope)

    suspend fun fetchAllUsersList(): Flow<List<UserDomain>> = fetchUserListUsecase(currentUserId())

    suspend fun fetchChatroomList(): Flow<List<ChatroomDomain>> = fetchChatRoomListUsecase(currentUserId())

    fun signIn(email: String, password: String, listener: SignInListener) {
        signInUsecase(email, password, listener)
    }

    fun sendVerificationEmail(
        email: String,
        password: String,
        listener: EmailVerificationSendListener
    ) {
        sendEmailVerificationUsecase(email, password, listener)
    }

    fun signUp(name: String, listener: EmailVerifyListener) {
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

    fun downloadProfileImage(userId: String, fileDownloadListener: FileDownloadListener) {
        downloadProfileImageUsecase(userId, fileDownloadListener)
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
        yourId: String,
        time: String,
        onSuccess: (String) -> Unit,
        onFail: () -> Unit,
        enter: Boolean
    ) {
        updateChatRoomUsecase(currentUserId(), yourId, time, onSuccess, onFail, enter)
    }

    fun fetchMessagesFromRemoteDB(chatroom: String, coroutineScope: CoroutineScope) {
        fetchMessagesFromRemoteDBUsecase(chatroom, coroutineScope)
    }

    companion object {
        private var currentUserId: String = ""
        fun currentUserId() = currentUserId
        fun setCurrentUserId(id: String) {
            currentUserId = id
        }
    }
}