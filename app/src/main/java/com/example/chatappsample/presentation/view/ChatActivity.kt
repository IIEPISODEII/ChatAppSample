package com.example.chatappsample.presentation.view

import android.app.Activity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatappsample.R
import com.example.chatappsample.databinding.ActivityChatBinding
import com.example.chatappsample.domain.dto.Message
import com.example.chatappsample.presentation.view.adapter.MessageAdapter
import com.example.chatappsample.presentation.viewmodel.ChatViewModel
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    lateinit var chatViewModel: ChatViewModel
    private lateinit var mBinding: ActivityChatBinding

    private val chattingRecyclerView by lazy { this.findViewById<RecyclerView>(R.id.rv_chat_recyclerview) }
    private val messageBox by lazy { this.findViewById<TextInputEditText>(R.id.et_chat_messagebox) }
    private val sendMessageButton by lazy { this.findViewById<ImageButton>(R.id.imgbtn_send_message) }

    private lateinit var messageAdapter: MessageAdapter
    var receiverRoom: String? = null
    var senderRoom: String? = null
    private var messageText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_chat)
        mBinding.viewmodel = chatViewModel
        mBinding.lifecycleOwner = this

        // get intent data from previous activity
        val userName = intent.getStringExtra(OTHER_NAME)?.let {
            supportActionBar?.title = it
        }
        val receiverUID = intent.getStringExtra(OTHER_UID)
        val senderUID = intent.getStringExtra(CURRENT_UID)
        senderRoom = receiverUID + senderUID
        receiverRoom = senderUID + receiverUID

        chatViewModel.getReceivedMessage(receiverRoom!!)
        chatViewModel.messageTxt.observe(this) { messageText = it }


        // Adding data to Recyclerview
        messageAdapter = MessageAdapter(messageList = listOf(), senderUID = senderUID ?: "")
        chattingRecyclerView.adapter = messageAdapter
        chattingRecyclerView.layoutManager = LinearLayoutManager(this)
        chatViewModel.messagesList.observe(this) {
            messageAdapter.messageList = it
            messageAdapter.notifyDataSetChanged()
        }


        // Send the message to db
        sendMessageButton.setOnClickListener {

            if (mBinding.etChatMessagebox.text!!.isEmpty()) return@setOnClickListener

            val sdf = SimpleDateFormat(
                "yyyy-MM-dd hh:mm",
                Locale.KOREA
            ).format(Date(System.currentTimeMillis()))
            val messageObject = Message(
                message = messageText,
                senderId = senderUID ?: "",
                sentTime = sdf
            )

            chatViewModel.sendMessage(message = messageObject, senderChatRoom = senderRoom!!, receiverChatRoom = receiverRoom!!)
            messageBox.setText("")
        }

        chattingRecyclerView.setOnClickListener {
            val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    companion object {
        const val OTHER_NAME = "selected_name"
        const val OTHER_UID = "selected_uid"
        const val CURRENT_UID = "current_uid"
    }
}