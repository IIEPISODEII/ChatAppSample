package com.example.chatappsample.presentation.view

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatappsample.Application
import com.example.chatappsample.R
import com.example.chatappsample.domain.dto.Message
import com.example.chatappsample.presentation.view.adapter.MessageAdapter
import com.example.chatappsample.presentation.viewmodel.ChatViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    lateinit var chatViewModel: ChatViewModel

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

        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        val userName = intent.getStringExtra("name")
        val receiverUid = intent.getStringExtra("sender_uid")
        val senderUid = chatViewModel.getCurrentUser()?.uid


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

            messageBox.setText("")
        }

        chattingRecyclerView.setOnClickListener {
            val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }

        // Adding data to Recyclerview

    }
}