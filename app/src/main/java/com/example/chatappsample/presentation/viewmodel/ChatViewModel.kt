package com.example.chatappsample.presentation.viewmodel

import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatappsample.domain.dto.Message
import com.example.chatappsample.domain.dto.User
import com.example.chatappsample.domain.usecase.GetCurrentUserUsecase
import com.example.chatappsample.domain.usecase.GetReceivedMessageUsecase
import com.example.chatappsample.domain.usecase.SendMessageUsecase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUsecase: SendMessageUsecase,
    private val receivedMessageUsecase: GetReceivedMessageUsecase,
    private val getCurrentUserUsecase: GetCurrentUserUsecase
) : ViewModel(), Observable {

    @Bindable
    val messageTxt = MutableLiveData<String>()

    fun sendMessage(
        message: Message,
        senderChatRoom: String,
        receiveChatRoom: String
    ) {
        sendMessageUsecase.sendMessage(
            message = message,
            senderChatRoom = senderChatRoom,
            receiverChatRoom = receiveChatRoom
        )
    }

    fun getReceivedMessage(chatRoom: String): ArrayList<Message> {
        return receivedMessageUsecase.getReceivedMessage(chatRoom)
    }

    fun getCurrentUser(): User? {
        return getCurrentUserUsecase.getCurrentUser()
    }


    private val callbacks: PropertyChangeRegistry by lazy { PropertyChangeRegistry() }
    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        callbacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        callbacks.remove(callback)
    }
}