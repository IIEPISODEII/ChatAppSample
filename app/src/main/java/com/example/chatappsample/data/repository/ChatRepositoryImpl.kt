package com.example.chatappsample.data.repository

import android.annotation.SuppressLint
import com.example.chatappsample.domain.dto.Message
import com.example.chatappsample.domain.repository.ChatRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase
) : ChatRepository {

    override fun getReceivedMessage(
        chatRoom: String,
        event: () -> Unit
    ): ArrayList<Message> {
        val messageList = ArrayList<Message>()

        firebaseDatabase.reference.child("user").child(chatRoom).child("messages")
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {

                    messageList.clear()

                    for (postSnapshot in snapshot.children) {
                        val msg = postSnapshot.getValue(Message::class.java)

                        messageList.add(msg!!)
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        event()
        return messageList
    }

    override fun sendMessage(
        message: Message,
        senderChatRoom: String,
        receiverChatRoom: String,
        sendListener: () -> Unit
    ) {
        firebaseDatabase.reference.child("messages").child(senderChatRoom).child("messages")
            .push()
            .setValue(message)
            .addOnSuccessListener {
                firebaseDatabase.reference.child("chats").child(receiverChatRoom)
                    .child("messages").push()
                    .setValue(message)
            }

        sendListener()
    }
}