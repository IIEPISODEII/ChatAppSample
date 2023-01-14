package com.example.chatappsample.data.repository

import android.net.Uri
import android.util.Log
import com.example.chatappsample.data.entity.MessageData
import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
import com.example.chatappsample.domain.`interface`.OnFirebaseCommunicationListener
import com.example.chatappsample.domain.dto.MessageDomain
import com.example.chatappsample.domain.repository.ChatRepository
import com.example.chatappsample.util.TEN_MEGABYTE
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storageMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseStorage: FirebaseStorage,
    private val roomDB: AppDatabase
) : ChatRepository {

    private val db = firebaseDatabase.reference

    override fun fetchMessagesFromExternalDB(
        chatRoom: String,
        coroutineScope: CoroutineScope
    ) {

        if (mMessageCoroutineScope != null && mMessageCoroutineScope == coroutineScope) return

        if (mMessageChildEventListener != null) {
            db
                .child(FIREBASE_FIRST_CHILD_CHATS)
                .child(chatRoom)
                .child(FIREBASE_SECOND_CHILD_MESSAGES)
                .removeEventListener(mMessageChildEventListener!!)
        }

        mMessageCoroutineScope = coroutineScope
        mMessageChildEventListener = object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                mMessageCoroutineScope!!.launch(Dispatchers.IO) {
                    val receivedMessageData = snapshot.getValue(MessageData::class.java)
                    receivedMessageData?.chatRoom = chatRoom
                    if (receivedMessageData != null) {
                        roomDB
                            .getMessageDao()
                            .insertMessage(receivedMessageData)
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
                Log.d("ERROR:", error.message)
            }
        }

        db
            .child(FIREBASE_FIRST_CHILD_CHATS)
            .child(chatRoom)
            .child(FIREBASE_SECOND_CHILD_MESSAGES)
            .addChildEventListener(mMessageChildEventListener!!)
    }

    override suspend fun fetchMessagesFromRoomDBAsFlow(
        chatRoom: String,
        queriesSize: Int
    ): Flow<List<MessageDomain?>> {
        return roomDB.getMessageDao()
            .fetchRecentlyReceivedMessagesAsFlow(chatRoom, queriesSize)
            .map { list ->
                list.map {
                    it?.toDomain()
                }
            }
    }

    override suspend fun fetchMessagesFromRoomDB(
        chatRoom: String,
        queriesSize: Int,
        offset: Int
    ): List<MessageDomain?> {
        return roomDB.getMessageDao()
            .fetchRecentlyReceivedMessages(chatRoom, queriesSize, offset)
            .map { messageData ->
                messageData?.toDomain()
            }
    }

    override suspend fun sendMessage(
        message: MessageDomain,
        chatRoom: String,
        onFirebaseCommunicationListener: OnFirebaseCommunicationListener
    ) {
        db
            .child(FIREBASE_FIRST_CHILD_CHATS)
            .child(chatRoom)
            .child(FIREBASE_SECOND_CHILD_MESSAGES)
            .push()
            .setValue(message)
            .addOnSuccessListener {
                onFirebaseCommunicationListener.onSuccess()
            }
            .addOnCanceledListener {
                onFirebaseCommunicationListener.onFailure()
            }
    }

    override suspend fun uploadFile(
        message: MessageDomain,
        chatRoom: String,
        onFirebaseCommunicationListener: OnFirebaseCommunicationListener
    ) {
        val messageData = MessageData(
            messageId = message.messageId,
            chatRoom = chatRoom,
            type = message.messageType,
            senderId = message.senderId,
            message = message.message,
            sentTime = message.sentTime
        )

        val metadata = storageMetadata {
            contentType = "image/jpeg"
        }
        firebaseStorage.reference
            .child("images/${messageData.senderId}/${messageData.sentTime}/${messageData.message}")
            .putFile(Uri.parse(messageData.message), metadata)
            .addOnSuccessListener {
                firebaseDatabase.reference.child(FIREBASE_FIRST_CHILD_CHATS)
                    .child(chatRoom)
                    .child(FIREBASE_SECOND_CHILD_MESSAGES)
                    .push()
                    .setValue(messageData)
                    .addOnSuccessListener {
                        onFirebaseCommunicationListener.onSuccess()
                    }
            }
            .addOnCanceledListener {
                onFirebaseCommunicationListener.onFailure()
            }
    }

    override fun downloadFile(
        message: MessageDomain,
        onFileDownloadListener: OnFileDownloadListener
    ) {
        val messageData = MessageData(
            messageId = message.messageId,
            type = message.messageType,
            senderId = message.senderId,
            message = message.message,
            sentTime = message.sentTime,
            chatRoom = ""
        )

        firebaseStorage
            .reference
            .child("images/${messageData.senderId}/${messageData.sentTime}/${messageData.message}")
            .getBytes(TEN_MEGABYTE)
            .addOnSuccessListener {
                onFileDownloadListener.onSuccess(it)
            }
            .addOnFailureListener {
                onFileDownloadListener.onFailure(it)
                it.printStackTrace()
            }
    }

    override suspend fun fetchLastMessageOfChatRoom(chatRoom: String): Flow<MessageDomain?> {
        println("이벤트 호출")
        return roomDB.getMessageDao().fetchLastReceivedMessages(chatRoom)
            .map { messageData ->
                println("--마지막 메시지-- $messageData")
                messageData?.toDomain()
            }
    }

    companion object {
        const val FIREBASE_FIRST_CHILD_CHATS = "chats"
        const val FIREBASE_SECOND_CHILD_MESSAGES = "messages"

        private var mMessageChildEventListener: ChildEventListener? = null
        private var mMessageCoroutineScope: CoroutineScope? = null

        fun initializeOverlapCheck() {
            mMessageChildEventListener = null
            mMessageCoroutineScope = null
        }
    }
}