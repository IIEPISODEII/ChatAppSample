package com.example.chatappsample.presentation.viewmodel

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
) : ViewModel() {

    private val _messageTxt = MutableLiveData<String>()
    val messageTxt: LiveData<String>
        get() = _messageTxt

    fun sendMessage(
        message: Message,
        senderChatRoom: String,
        receiveChatRoom: String,
        sendListener: () -> Unit
    ) {
        sendMessageUsecase.sendMessage(
            message = message,
            senderChatRoom = senderChatRoom,
            receiverChatRoom = receiveChatRoom,
            sendListener = sendListener
        )
    }

    fun getReceivedMessage(
        chatRoom: String,
        receiveListener: () -> Unit
    ): ArrayList<Message> {
        return receivedMessageUsecase.getReceivedMessage(
            chatRoom,
            receiveListener
        )
    }

    fun getCurrentUser(): User? {
        return getCurrentUserUsecase.getCurrentUser {  }
    }
}