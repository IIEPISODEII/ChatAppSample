package com.example.chatappsample.presentation.viewmodel

import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatappsample.domain.`interface`.FileDownloadListener
import com.example.chatappsample.domain.`interface`.FileUploadListener
import com.example.chatappsample.domain.dto.ChatroomDomain
import com.example.chatappsample.domain.dto.MessageDomain
import com.example.chatappsample.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUsecase: SendMessageUsecase,
    private val fetchMessagesFromRemoteDBUsecase: FetchMessagesFromRemoteDBUsecase,
    private val fetchMessageFromRoomDBUsecase: FetchMessagesFromLocalDBUsecase,
    private val fetchChatroomListFromLocalDBUsecase: FetchChatroomListFromLocalDBUsecase,
    private val fetchReaderLogFromRemoteDBUsecase: FetchReaderLogFromRemoteDBUsecase,
    private val fetchReaderLogAsFlowUsecase: FetchChatroomListFromLocalDBAsFlowUsecase,
    private val fetchLastMessageUsecase: FetchLastMessageUsecase,
    private val uploadFileUsecase: UploadFileUsecase,
    private val downloadFileUsecase: DownloadFileUsecase,
    private val downloadProfileImageUsecase: DownloadProfileImageUsecase,
    private val updateChatRoomUsecase: UpdateChatroomUsecase
) : ViewModel(), Observable {

    @Bindable
    val messageTxt = MutableLiveData<String>()

    private val _messageDomainList = MutableLiveData<MutableList<MessageDomain>>(mutableListOf())
    val messageDomainList: LiveData<MutableList<MessageDomain>>
        get() = _messageDomainList
    private val newlyMessageDomainList = mutableListOf<MessageDomain>()
    private val preMessageDomainList = mutableListOf<MessageDomain>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            fetchMessagesFromRemoteDB(chatRoomId)
            fetchMessagesFromLocalDB(chatRoomId)
            fetchLastMessageFromLocalDB(chatRoomId)
        }
    }

    fun sendMessage(
        message: MessageDomain,
        chatRoom: String,
        fileUploadListener: FileUploadListener
    ) {
        sendMessageUsecase(
            message = message,
            chatRoom = chatRoom,
            fileUploadListener = fileUploadListener
        )
    }

    private fun fetchMessagesFromRemoteDB(chatRoom: String) {
        fetchMessagesFromRemoteDBUsecase(chatRoom, viewModelScope)
    }

    fun setPreMessageList(messageDomainList: List<MessageDomain>) {
        this.preMessageDomainList.clear()
        this.preMessageDomainList.addAll(messageDomainList)
    }

    fun getPreMessageList() = this.preMessageDomainList

    private var receivedMessages = 1
    private val basicMessageDownloadSize = 20
    private var offsetSize = 0
    suspend fun fetchMessagesFromLocalDB(chatRoom: String) {
        offsetSize += basicMessageDownloadSize

        val oldMessages = fetchMessageFromRoomDBUsecase(chatRoom, basicMessageDownloadSize, offsetSize - basicMessageDownloadSize + receivedMessages)

        newlyMessageDomainList.addAll(0, oldMessages)
        withContext(Dispatchers.IO) { if (newlyMessageDomainList.isEmpty()) fetchLastMessageFromLocalDB(chatRoom) }
        _messageDomainList.postValue(newlyMessageDomainList)
    }

    private suspend fun fetchLastMessageFromLocalDB(chatRoom: String) {
        addMessageAfterLaunchingActivity()
        fetchLastMessageUsecase(chatRoom).collect {
            if (it == null) return@collect

            newlyMessageDomainList.add(it)
            withContext(Dispatchers.Main) { _messageDomainList.postValue(newlyMessageDomainList) }
        }
    }

    private fun addMessageAfterLaunchingActivity() {
        receivedMessages++
    }

    private val callbacks: PropertyChangeRegistry by lazy { PropertyChangeRegistry() }
    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        callbacks.add(callback)
    }


    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        callbacks.remove(callback)
    }

    suspend fun uploadFile(
        messageDomain: MessageDomain,
        chatRoom: String,
        fileUploadListener: FileUploadListener
    ) {
        uploadFileUsecase(
            messageDomain,
            chatRoom,
            fileUploadListener
        )
    }

    fun downloadFile(messageDomain: MessageDomain, fileDownloadListener: FileDownloadListener) {
        downloadFileUsecase(messageDomain, fileDownloadListener)
    }

    fun downloadProfileImage(userID: String, fileDownloadListener: FileDownloadListener) {
        downloadProfileImageUsecase(userID, fileDownloadListener)
    }

    fun updateChatRoom(yourId: String, time: String, onSuccess: (String) -> Unit, onFail: () -> Unit, enter: Boolean) {
        updateChatRoomUsecase(UserViewModel.currentUserId(), yourId, time, onSuccess, onFail, enter)
    }

    fun fetchReaderLogFromRemoteDB(chatroomId: String, coroutineScope: CoroutineScope) {
        fetchReaderLogFromRemoteDBUsecase(chatroomId, UserViewModel.currentUserId(), coroutineScope)
    }

    suspend fun fetchChatroomInformation(userId: String): Flow<List<ChatroomDomain>> = fetchChatroomListFromLocalDBUsecase(userId).stateIn(viewModelScope)

    suspend fun fetchReaderLogAsFlow(): Flow<List<ChatroomDomain.ReaderLogDomain>> {
        return fetchReaderLogAsFlowUsecase(chatRoomId)
    }

    companion object {
        private var chatRoomId = ""

        fun setReceiverRoom(room: String) {
            chatRoomId = room
        }
    }
}