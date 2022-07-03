package com.example.chatappsample.presentation.viewmodel

import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatappsample.domain.`interface`.OnGetDataListener
import com.example.chatappsample.domain.dto.Message
import com.example.chatappsample.domain.dto.User
import com.example.chatappsample.domain.repository.UserRepository
import com.example.chatappsample.domain.usecase.GetCurrentUserUsecase
import com.example.chatappsample.domain.usecase.GetReceivedMessageUsecase
import com.example.chatappsample.domain.usecase.SendMessageUsecase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
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
        receiverChatRoom: String
    ): Boolean {
        return sendMessageUsecase.sendMessage(
            message = message,
            senderChatRoom = senderChatRoom,
            receiverChatRoom = receiverChatRoom
        )
    }


    private val _messagesList: MutableLiveData<List<Message>> = MutableLiveData()
    val messagesList: LiveData<List<Message>>
        get() = _messagesList

    fun getReceivedMessage(chatRoom: String) {
        val mListener = object: OnGetDataListener {
            override fun onSuccess(dataSnapshot: DataSnapshot) {
                val msgList = mutableListOf<Message>()
                println("dataSnapshot: $dataSnapshot")
                for (snapshot in dataSnapshot.children) {

                    val message = snapshot.getValue(Message::class.java)!!
                    msgList.add(message)
                }
                _messagesList.value = msgList.toList()
            }

            override fun onStart() {
            }

            override fun <T> onFailure(error: T) {
                if (error is DatabaseError) throw Exception(error.message)
            }

        }
        println(chatRoom)
        receivedMessageUsecase.getReceivedMessage(chatRoom, mListener)
    }


    private val callbacks: PropertyChangeRegistry by lazy { PropertyChangeRegistry() }
    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        callbacks.add(callback)
    }


    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        callbacks.remove(callback)
    }
}