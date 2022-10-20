package com.example.chatappsample.data.repository

import android.net.Uri
import android.util.Log
import com.example.chatappsample.data.entity.MessageEntity
import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
import com.example.chatappsample.domain.`interface`.OnFirebaseCommunicationListener
import com.example.chatappsample.domain.dto.Message
import com.example.chatappsample.domain.repository.ChatRepository
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
    private val messageRoomDB: AppDatabase
) : ChatRepository {

    companion object {
        const val FIREBASE_FIRST_CHILD = "chats"
        const val FIREBASE_SECOND_CHILD = "messages"
    }

    override suspend fun fetchMessagesFromExternalDB(
        chatRoom: String,
        coroutineScope: CoroutineScope
    ) {
        firebaseDatabase
            .reference
            .child(FIREBASE_FIRST_CHILD)
            .child(chatRoom)
            .child(FIREBASE_SECOND_CHILD)
            .addChildEventListener(object: ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    coroutineScope.launch(Dispatchers.IO) {
                        val receivedMessage = snapshot.getValue(Message::class.java)
                        if (receivedMessage != null) {
                            messageRoomDB
                                .getMessageDao()
                                .insertMessage(receivedMessage.toMessageEntity(chatRoom))
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
    ): Flow<List<Message>> {
        return messageRoomDB.getMessageDao().fetchRecentlyReceivedMessagesAsFlow(chatRoom, queriesSize).map { messageEntityList -> messageEntityList.map{ it.toMessageDTO() } }
    }

    override suspend fun fetchMessagesFromRoomDB(
        chatRoom: String,
        queriesSize: Int,
        offset: Int
    ): List<Message> {
        return messageRoomDB.getMessageDao().fetchRecentlyReceivedMessages(chatRoom, queriesSize, offset).map { it.toMessageDTO() }
    }

    override suspend fun sendMessage(
        message: Message,
        myChatRoom: String,
        yourChatRoom: String,
        onFirebaseCommunicationListener: OnFirebaseCommunicationListener
    ) {
        firebaseDatabase.reference.child(FIREBASE_FIRST_CHILD)
            .child(myChatRoom)
            .child(FIREBASE_SECOND_CHILD)
            .push()
            .setValue(message)
            .addOnSuccessListener {
                firebaseDatabase.reference
                    .child(FIREBASE_FIRST_CHILD)
                    .child(yourChatRoom)
                    .child(FIREBASE_SECOND_CHILD)
                    .push()
                    .setValue(message)
                onFirebaseCommunicationListener.onSuccess()
            }
            .addOnCanceledListener {
                onFirebaseCommunicationListener.onFailure()
            }
    }

    override suspend fun uploadFile(
        message: Message,
        myChatRoom: String,
        yourChatRoom: String,
        onFirebaseCommunicationListener: OnFirebaseCommunicationListener
    ) {
        val metadata = storageMetadata {
            contentType = "image/jpeg"
        }
        firebaseStorage.reference
            .child("images/${message.senderId}/${message.sentTime}/${message.message}")
            .putFile(Uri.parse(message.message), metadata)
            .addOnSuccessListener {
                firebaseDatabase.reference.child(FIREBASE_FIRST_CHILD)
                    .child(myChatRoom)
                    .child(FIREBASE_SECOND_CHILD)
                    .push()
                    .setValue(message)
                    .addOnSuccessListener {
                        firebaseDatabase.reference
                            .child(FIREBASE_FIRST_CHILD)
                            .child(yourChatRoom)
                            .child(FIREBASE_SECOND_CHILD)
                            .push()
                            .setValue(message)

                        onFirebaseCommunicationListener.onSuccess()
                    }
            }
            .addOnCanceledListener {
                onFirebaseCommunicationListener.onFailure()
            }
    }

    private val TEN_MEGABYTE = 10L * 1024L * 1024L

    override fun downloadFile(
        message: Message,
        onFileDownloadListener: OnFileDownloadListener
    ) {
        firebaseStorage
            .reference
            .child("images/${message.senderId}/${message.sentTime}/${message.message}")
            .getBytes(TEN_MEGABYTE)
            .addOnSuccessListener {
                onFileDownloadListener.onSuccess(it)
            }
            .addOnFailureListener {
                onFileDownloadListener.onFailure(it)
                it.printStackTrace()
            }
    }

    override suspend fun takeLastMessageOfChatRoom(chatRoom: String): Flow<Message> {
        return messageRoomDB
            .getMessageDao()
            .fetchLastReceivedMessages(chatRoom)
            .map {
                value: MessageEntity? ->
                value?.toMessageDTO() ?: Message()
        }
    }
}