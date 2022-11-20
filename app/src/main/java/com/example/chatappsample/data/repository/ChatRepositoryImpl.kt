package com.example.chatappsample.data.repository

import android.net.Uri
import android.util.Log
import com.example.chatappsample.data.entity.MessageData
import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
import com.example.chatappsample.domain.`interface`.OnFirebaseCommunicationListener
import com.example.chatappsample.domain.dto.ChatRoomDomain
import com.example.chatappsample.domain.dto.MessageDomain
import com.example.chatappsample.domain.repository.ChatRepository
import com.example.chatappsample.util.TEN_MEGABYTE
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storageMetadata
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseStorage: FirebaseStorage,
    private val roomDB: AppDatabase
) : ChatRepository {

    private val db = firebaseDatabase.reference

    override suspend fun fetchMessagesFromExternalDB(
        chatRoom: String,
        coroutineScope: CoroutineScope
    ) {
        db
            .child(FIREBASE_FIRST_CHILD_CHATS)
            .child(chatRoom)
            .child(FIREBASE_SECOND_CHILD_MESSAGES)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    coroutineScope.launch(Dispatchers.IO) {
                        val receivedMessageData = snapshot.getValue(MessageData::class.java)
                        receivedMessageData?.chatRoom = chatRoom
                        if (receivedMessageData != null) {
                            roomDB
                                .getMessageDao()
                                .insertMessage(receivedMessageData)
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                }

                override fun onChildRemoved(snapshot: DataSnapshot) {

                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ERROR:", error.message)
                }

            })
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

    override suspend fun takeLastMessageOfChatRoom(chatRoom: String): Flow<MessageDomain?> {
        return roomDB.getMessageDao().fetchLastReceivedMessages(chatRoom)
            .map { messageData ->
                messageData?.toDomain()
            }
    }

    override suspend fun fetchChatRoomFromDB(chatRoomId: String): Flow<List<ChatRoomDomain.ReaderLog>> {
        println("chatRoomId in RepoImpl: $chatRoomId")
        return roomDB.getChatRoomDao().fetchReaderLogs(targetChatRoom = chatRoomId).map { list ->
            list.map {
                println("ReaderLog in RepoImpl : $list")
                ChatRoomDomain.ReaderLog(it.participantsId, it.participationTime)
            }
        }
    }

    companion object {
        const val FIREBASE_FIRST_CHILD_CHATS = "chats"
        const val FIREBASE_SECOND_CHILD_MESSAGES = "messages"
    }
}