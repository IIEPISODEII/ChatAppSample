package com.example.chatappsample.presentation.view

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatappsample.Application
import com.example.chatappsample.R
import com.example.chatappsample.model.Message
import com.example.chatappsample.presentation.view.adapter.MessageAdapter
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    private val chattingRecyclerView by lazy { this.findViewById<RecyclerView>(R.id.rv_chat_recyclerview) }
    private val messageBox by lazy { this.findViewById<TextInputEditText>(R.id.et_chat_messagebox) }
    private val sendMessageButton by lazy { this.findViewById<ImageButton>(R.id.imgbtn_send_message) }

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    var receiverRoom: String? = null
    var senderRoom: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val userName = intent.getStringExtra("name")
        val receiverUid = intent.getStringExtra("uid")
        val senderUid = Application.mFirebaseAuth.currentUser?.uid


        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        messageList = ArrayList()
        supportActionBar?.title = userName
        messageAdapter = MessageAdapter(messageList = messageList)

        chattingRecyclerView.adapter = messageAdapter
        chattingRecyclerView.layoutManager = LinearLayoutManager(this)

        // Adding the message to db
        sendMessageButton.setOnClickListener {

            val message = messageBox.text.toString()
            if (message.isEmpty()) return@setOnClickListener

            val sdf = SimpleDateFormat(
                "yyyy-MM-dd hh:mm",
                Locale.KOREA
            ).format(Date(System.currentTimeMillis()))
            val messageObject = Message(
                message = message,
                senderId = senderUid!!,
                sentTime = sdf
            )

            Application.mFbDatabaseRef.child("chats").child(senderRoom!!).child("messages").push()
                .setValue(messageObject)
                .addOnSuccessListener {
                    Application.mFbDatabaseRef.child("chats").child(receiverRoom!!)
                        .child("messages").push()
                        .setValue(messageObject)

                }

            messageBox.setText("")
        }

        chattingRecyclerView.setOnClickListener {
            val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }

        // Adding data to Recyclerview
        Application.mFbDatabaseRef.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object: ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {

                    messageList.clear()

                    for (postSnapshot in snapshot.children) {
                        val msg = postSnapshot.getValue(Message::class.java)

                        messageList.add(msg!!)
                    }
                    messageAdapter.notifyDataSetChanged()

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }
}