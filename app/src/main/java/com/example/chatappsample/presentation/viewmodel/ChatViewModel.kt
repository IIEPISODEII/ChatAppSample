package com.example.chatappsample.presentation.viewmodel

import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
import com.example.chatappsample.domain.`interface`.OnFirebaseCommunicationListener
import com.example.chatappsample.domain.dto.ChatRoomDomain
import com.example.chatappsample.domain.dto.MessageDomain
import com.example.chatappsample.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUsecase: SendMessageUsecase,
    private val fetchMessagesFromExternalDBUsecase: ReceiveMessagesFromExternalDBUsecase,
    private val fetchMessageFromRoomDBUsecase: FetchMessagesFromRoomDBUsecase,
    private val fetchLastMessageUsecase: FetchLastMessageUsecase,
    private val uploadFileUsecase: UploadFileUsecase,
    private val downloadFileUsecase: DownloadFileUsecase,
    private val downloadProfileImageUsecase: DownloadProfileImageUsecase,
    private val fetchReaderLogAsFlowUsecase: FetchReaderLogAsFlowUsecase,
    private val updateChatRoomUsecase: UpdateChatRoomUsecase
) : ViewModel(), Observable {

    companion object {
        private var chatRoomId = ""

        fun setReceiverRoom(room: String) {
            chatRoomId = room
        }
    }

    @Bindable
    val messageTxt = MutableLiveData<String>()

    private val _messageDomainList = MutableLiveData<MutableList<MessageDomain>>(mutableListOf())
    val messageDomainList: LiveData<MutableList<MessageDomain>>
        get() = _messageDomainList
    private val newlyMessageDomainList = mutableListOf<MessageDomain>()
    private val preMessageDomainList = mutableListOf<MessageDomain>()

    private val _readerLogs = MutableLiveData<List<ChatRoomDomain.ReaderLog>>()
    val readerLogs: LiveData<List<ChatRoomDomain.ReaderLog>>
        get() = _readerLogs

    init {
        viewModelScope.launch(Dispatchers.IO) {
            fetchMessagesFromExternalDB(chatRoomId)
            fetchMessagesFromRoomDB(chatRoomId)
            fetchLastMessageFromRoomDB(chatRoomId)
        }
        viewModelScope.launch(Dispatchers.IO) {
            fetchReaderLogsFromRoomDB(chatRoomId)
        }
    }

    suspend fun sendMessage(
        message: MessageDomain,
        chatRoom: String,
        onFirebaseCommunicationListener: OnFirebaseCommunicationListener
    ) {
        sendMessageUsecase(
            message = message,
            chatRoom = chatRoom,
            onFirebaseCommunicationListener = onFirebaseCommunicationListener
        )
    }

    private suspend fun fetchMessagesFromExternalDB(chatRoom: String) {
        fetchMessagesFromExternalDBUsecase(chatRoom, viewModelScope)
    }

    fun setPreMessageList(messageDomainList: List<MessageDomain>) {
        this.preMessageDomainList.clear()
        this.preMessageDomainList.addAll(messageDomainList)
    }

    fun getPreMessageList() = this.preMessageDomainList

    private var receivedMessages = 1
    private val basicMessageDownloadSize = 20
    private var offsetSize = 0
    suspend fun fetchMessagesFromRoomDB(chatRoom: String) {
        offsetSize += basicMessageDownloadSize

        val oldMessages = fetchMessageFromRoomDBUsecase(chatRoom, basicMessageDownloadSize, offsetSize - basicMessageDownloadSize + receivedMessages)

        newlyMessageDomainList.addAll(0, oldMessages)
        if (newlyMessageDomainList.isEmpty()) fetchLastMessageFromRoomDB(chatRoom)
        _messageDomainList.postValue(newlyMessageDomainList)
    }

    private suspend fun fetchLastMessageFromRoomDB(chatRoom: String) {
        addMessageAfterLaunchingActivity()
        fetchLastMessageUsecase(chatRoom).stateIn(viewModelScope).collect {
            if (it == null) return@collect

            newlyMessageDomainList.add(it)
            _messageDomainList.postValue(newlyMessageDomainList)
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
        onFirebaseCommunicationListener: OnFirebaseCommunicationListener
    ) {
        uploadFileUsecase(
            messageDomain,
            chatRoom,
            onFirebaseCommunicationListener
        )
    }

    fun downloadFile(messageDomain: MessageDomain, onFileDownloadListener: OnFileDownloadListener) {
        downloadFileUsecase(messageDomain, onFileDownloadListener)
    }

    fun downloadProfileImage(userID: String, onFileDownloadListener: OnFileDownloadListener) {
        downloadProfileImageUsecase(userID, onFileDownloadListener)
    }

    private suspend fun fetchReaderLogsFromRoomDB(chatRoomId: String) {
        fetchReaderLogAsFlowUsecase(chatRoomId).stateIn(viewModelScope).collect {
            _readerLogs.postValue(it)
        }
    }

    fun updateChatRoom(myId: String, yourId: String, time: String, onSuccess: (String) -> Unit, onFail: () -> Unit, enter: Boolean, coroutineScope: CoroutineScope = viewModelScope) {
        updateChatRoomUsecase(myId, yourId, time, onSuccess, onFail, enter, coroutineScope)
    }
}