package com.example.chatappsample.presentation.viewmodel

import android.net.Uri
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
import com.example.chatappsample.domain.`interface`.OnFirebaseCommunicationListener
import com.example.chatappsample.domain.`interface`.OnGetDataListener
import com.example.chatappsample.domain.dto.Message
import com.example.chatappsample.domain.usecase.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUsecase: SendMessageUsecase,
    private val receiveAllMessagesUsecase: ReceiveAllMessagesUsecase,
    private val receivedMessageUsecase: GetReceivedMessageUsecase,
    private val uploadFileUsecase: UploadFileUsecase,
    private val downloadFileUsecase: DownloadFileUsecase,
    private val getLastMessageIndexUsecase: GetLastMessageIndexUsecase,
    private val saveLastMessageIndexUsecase: SaveLastMessageIndexUsecase,
) : ViewModel(), Observable {

    @Bindable
    val messageTxt = MutableLiveData<String>()

    fun sendMessage(
        message: Message,
        senderChatRoom: String,
        receiverChatRoom: String,
        onFirebaseCommunicationListener: OnFirebaseCommunicationListener
    ): Boolean {
        return sendMessageUsecase.sendMessage(
            message = message,
            senderChatRoom = senderChatRoom,
            receiverChatRoom = receiverChatRoom,
            onFirebaseCommunicationListener = onFirebaseCommunicationListener
        )
    }

    private val msgList = mutableListOf<Message>()
    private val _messagesList: MutableLiveData<List<Message>> = MutableLiveData()
    val messagesList: LiveData<List<Message>>
        get() = _messagesList

    fun receiveAllMessages(chatRoom: String) {
        val listener = object : OnGetDataListener {
            override fun onSuccess(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach {
                    msgList.add(it.getValue(Message::class.java)!!)
                }
                _messagesList.postValue(msgList)
            }

            override fun onStart() {

            }

            override fun <T> onFailure(error: T) {
                if (error is DatabaseError) throw Exception(error.message)
            }
        }

        receiveAllMessagesUsecase(chatRoom, listener)
    }

    private var queriesSize = 0
    fun getReceivedMessage(chatRoom: String) {
        queriesSize += 30
        val mListener = object : OnGetDataListener {
            override fun onSuccess(dataSnapshot: DataSnapshot) {
                msgList.add(dataSnapshot.getValue(Message::class.java)!!)
                _messagesList.postValue(msgList.toList())
            }

            override fun onStart() {}

            override fun <T> onFailure(error: T) {
                if (error is DatabaseError) throw Exception(error.message)
            }

        }
        receivedMessageUsecase(chatRoom, queriesSize, mListener)
    }


    private val callbacks: PropertyChangeRegistry by lazy { PropertyChangeRegistry() }
    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        callbacks.add(callback)
    }


    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        callbacks.remove(callback)
    }


    private val _isOptionActivated = MutableLiveData(false)
    val isOptionActivated: LiveData<Boolean>
        get() = _isOptionActivated

    fun activateOption(value: Boolean) {
        _isOptionActivated.postValue(value)
    }

    fun getLastMessageIndex(chatRoom: String) = getLastMessageIndexUsecase(chatRoom)

    fun saveLastMessageIndex(chatRoom: String, index: Int) =
        saveLastMessageIndexUsecase(chatRoom, index)

    fun uploadFile(
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

    fun downloadFile(uri: Uri, onFileDownloadListener: OnFileDownloadListener) {
        downloadFileUsecase(uri, onFileDownloadListener)
    }
}