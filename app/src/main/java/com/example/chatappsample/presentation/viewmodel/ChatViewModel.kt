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
import com.example.chatappsample.domain.dto.Message
import com.example.chatappsample.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
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
    private val downloadProfileImageUsecase: DownloadProfileImageUsecase
) : ViewModel(), Observable {

    companion object {
        private var receiverRoom = ""

        fun setReceiverRoom(room: String) {
            receiverRoom = room
        }
    }

    @Bindable
    val messageTxt = MutableLiveData<String>()

    private val _messageList = MutableLiveData<MutableList<Message>>(mutableListOf())
    val messageList: LiveData<MutableList<Message>>
        get() = _messageList
    private val newlyMessageList = mutableListOf<Message>()
    private val preMessageList = mutableListOf<Message>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            fetchMessagesFromExternalDB(receiverRoom)
            fetchMessagesFromRoomDB(receiverRoom)
            fetchLastMessageFromRoomDB(receiverRoom)
        }
    }

    suspend fun sendMessage(
        message: Message,
        senderChatRoom: String,
        receiverChatRoom: String,
        onFirebaseCommunicationListener: OnFirebaseCommunicationListener
    ) {
        sendMessageUsecase(
            message = message,
            senderChatRoom = senderChatRoom,
            receiverChatRoom = receiverChatRoom,
            onFirebaseCommunicationListener = onFirebaseCommunicationListener
        )
    }

    private suspend fun fetchMessagesFromExternalDB(chatRoom: String) {
        fetchMessagesFromExternalDBUsecase(chatRoom, viewModelScope)
    }

    fun setPreMessageList(messageList: List<Message>) {
        this.preMessageList.clear()
        this.preMessageList.addAll(messageList)
    }

    fun getPreMessageList() = this.preMessageList

    private var receivedMessages = 1
    private val basicMessageDownloadSize = 20
    private var offsetSize = 0
    suspend fun fetchMessagesFromRoomDB(chatRoom: String) {
        offsetSize += basicMessageDownloadSize

        val oldMessages = fetchMessageFromRoomDBUsecase(chatRoom, basicMessageDownloadSize, offsetSize - basicMessageDownloadSize + receivedMessages)

        newlyMessageList.addAll(0, oldMessages)
        if (newlyMessageList.isEmpty()) fetchLastMessageFromRoomDB(chatRoom)
        _messageList.postValue(newlyMessageList)
    }

    private suspend fun fetchLastMessageFromRoomDB(chatRoom: String) {
        addMessageAfterLaunchingActivity()
        fetchLastMessageUsecase(chatRoom).stateIn(viewModelScope).collect {
            newlyMessageList.add(it)
            _messageList.postValue(newlyMessageList)
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
        message: Message,
        senderChatRoom: String,
        receiverChatRoom: String,
        onFirebaseCommunicationListener: OnFirebaseCommunicationListener
    ) {
        uploadFileUsecase(
            message,
            senderChatRoom,
            receiverChatRoom,
            onFirebaseCommunicationListener
        )
    }

    fun downloadFile(message: Message, onFileDownloadListener: OnFileDownloadListener) {
        downloadFileUsecase(message, onFileDownloadListener)
    }

    fun downloadProfileImage(userID: String, onFileDownloadListener: OnFileDownloadListener) {
        downloadProfileImageUsecase(userID, onFileDownloadListener)
    }
}