package com.example.chatappsample.presentation.viewmodel

import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
import com.example.chatappsample.domain.`interface`.OnFirebaseCommunicationListener
import com.example.chatappsample.domain.dto.Message
import com.example.chatappsample.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
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

    @Bindable
    val messageTxt = MutableLiveData<String>()


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

    suspend fun fetchMessagesFromExternalDB(chatRoom: String) {
        fetchMessagesFromExternalDBUsecase(chatRoom, viewModelScope)
    }

    private var messagesAfterLaunchingActivity = 1
    private var queriesSize = 0
    suspend fun fetchMessagesFromRoomDB(chatRoom: String): List<Message> {
        queriesSize += 50
        return fetchMessageFromRoomDBUsecase(chatRoom, queriesSize, messagesAfterLaunchingActivity)
    }

    suspend fun fetchLastMessageFromRoomDB(chatRoom: String, coroutineScope: CoroutineScope): StateFlow<Message> {
        return fetchLastMessageUsecase(chatRoom).stateIn(coroutineScope)
    }

    fun addMessageAfterLaunchingActivity() {
        messagesAfterLaunchingActivity++
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