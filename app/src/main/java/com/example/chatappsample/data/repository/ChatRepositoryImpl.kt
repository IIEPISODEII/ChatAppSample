package com.example.chatappsample.data.repository

import android.annotation.SuppressLint
import android.net.Uri
import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
import com.example.chatappsample.domain.`interface`.OnFirebaseCommunicationListener
import com.example.chatappsample.domain.`interface`.OnGetDataListener
import com.example.chatappsample.domain.dto.Message
import com.example.chatappsample.domain.repository.ChatRepository
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storageMetadata
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseStorage: FirebaseStorage
) : ChatRepository {

    companion object {
        const val FIREBASE_FIRST_CHILD = "chats"
        const val FIREBASE_SECOND_CHILD = "messages"
    }

    private var childEventListener: ChildEventListener? = null
    private var postQueriesSize: Int = 0

    override fun receiveAllMessages(
        chatRoom: String,
        listener: OnGetDataListener
    ) {
        firebaseDatabase
            .reference
            .child(FIREBASE_FIRST_CHILD)
            .child(chatRoom)
            .child(FIREBASE_SECOND_CHILD)
            .limitToLast(30)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listener.onSuccess(snapshot)

                    firebaseDatabase
                        .reference
                        .child(FIREBASE_FIRST_CHILD)
                        .child(chatRoom)
                        .child(FIREBASE_SECOND_CHILD)
                        .limitToLast(30)
                        .removeEventListener(this)
                }

                override fun onCancelled(error: DatabaseError) {
                    listener.onFailure(error)
                }
            })
    }

    override fun receiveAdditionalMessage(
        chatRoom: String,
        queriesSize: Int,
        listener: OnGetDataListener
    ) {
        if (this.childEventListener != null) {
            firebaseDatabase.reference
                .child(FIREBASE_FIRST_CHILD)
                .child(chatRoom)
                .child(FIREBASE_SECOND_CHILD)
                .limitToLast(postQueriesSize)
                .removeEventListener(this.childEventListener!!)
        }

        this.childEventListener = object: ChildEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                listener.onSuccess(snapshot)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
                listener.onFailure(error)
            }
        }
        listener.onStart()

        postQueriesSize = queriesSize
        firebaseDatabase
            .reference
            .child(FIREBASE_FIRST_CHILD)
            .child(chatRoom)
            .child(FIREBASE_SECOND_CHILD)
            .limitToLast(postQueriesSize)
            .addChildEventListener(this.childEventListener!!)
    }

    override fun sendMessage(
        message: Message,
        senderChatRoom: String,
        receiverChatRoom: String,
        onFirebaseCommunicationListener: OnFirebaseCommunicationListener
    ): Boolean {
        firebaseDatabase.reference.child(FIREBASE_FIRST_CHILD)
            .child(senderChatRoom)
            .child(FIREBASE_SECOND_CHILD)
            .push()
            .setValue(message)
            .addOnSuccessListener {
                firebaseDatabase.reference
                    .child(FIREBASE_FIRST_CHILD)
                    .child(receiverChatRoom)
                    .child(FIREBASE_SECOND_CHILD)
                    .push()
                    .setValue(message)
                onFirebaseCommunicationListener.onSuccess()
            }
            .addOnCanceledListener {
                onFirebaseCommunicationListener.onFailure()
            }

        return true
    }

    override fun uploadFile(
        message: Message,
        senderChatRoom: String,
        receiverChatRoom: String,
        onFirebaseCommunicationListener: OnFirebaseCommunicationListener
    ) {
        firebaseDatabase.reference.child(FIREBASE_FIRST_CHILD)
            .child(senderChatRoom)
            .child(FIREBASE_SECOND_CHILD)
            .push()
            .setValue(message)
            .addOnSuccessListener {
                firebaseDatabase.reference
                    .child(FIREBASE_FIRST_CHILD)
                    .child(receiverChatRoom)
                    .child(FIREBASE_SECOND_CHILD)
                    .push()
                    .setValue(message)

                val metadata = storageMetadata {
                    contentType = "image/jpeg"
                }
                firebaseStorage.reference
                    .child("images/${message.senderId}/${message.sentTime}/${message.messageIndex}")
                    .putFile(Uri.parse(message.imageUri), metadata)


                onFirebaseCommunicationListener.onSuccess()
            }
            .addOnCanceledListener {
                onFirebaseCommunicationListener.onFailure()
            }
    }

    private val TEN_MEGABYTE = 10L*1024L*1024L

    override fun downloadFile(
        uri: Uri,
        onFileDownloadListener: OnFileDownloadListener
    ) {
        firebaseStorage
            .reference
            .child("images/$uri")
            .getBytes(TEN_MEGABYTE)
//            .downloadUrl
            .addOnSuccessListener {
                onFileDownloadListener.onSuccess(it)
            }
            .addOnFailureListener {
                onFileDownloadListener.onFailure(it)
            }

    }
}