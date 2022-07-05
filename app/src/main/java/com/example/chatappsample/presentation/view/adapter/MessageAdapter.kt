package com.example.chatappsample.presentation.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatappsample.R
import com.example.chatappsample.domain.dto.Message
import com.google.android.material.textview.MaterialTextView

class MessageAdapter(var messageList: List<Message>, val senderUID: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val SENT_MESSAGE = 0
    private val RECEIVED_MESSAGE = 1
    private var sentMessageClickListener: MessageClickListener? = null
    private var receivedMessageClickListener: MessageClickListener? = null

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentMessage: MaterialTextView = itemView.findViewById(R.id.tv_sent_message)
        val sentTime: MaterialTextView = itemView.findViewById(R.id.tv_sent_message_time)

    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receivedMessage: MaterialTextView = itemView.findViewById(R.id.tv_received_message)
        val receivedTime: MaterialTextView = itemView.findViewById(R.id.tv_received_message_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == SENT_MESSAGE) {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.relativelayout_sent_message, null)
            SentMessageViewHolder(itemView = itemView)
        } else {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.relativelayout_received_message, null)
            ReceivedMessageViewHolder(itemView = itemView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.javaClass == SentMessageViewHolder::class.java) {
            (holder as SentMessageViewHolder).apply {
                sentMessage.text = messageList[position].message
                sentTime.text = messageList[position].sentTime
            }

            if (sentMessageClickListener != null) holder.itemView.setOnClickListener {
                sentMessageClickListener!!.onClick(it, position)
            }
        } else {
            (holder as ReceivedMessageViewHolder).apply {
                receivedMessage.text = messageList[position].message
                receivedTime.text = messageList[position].sentTime
            }

            if (receivedMessageClickListener != null) holder.itemView.setOnClickListener {
                receivedMessageClickListener!!.onClick(it, position)
            }
        }
    }

    override fun getItemCount(): Int = messageList.size

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]

        return if (senderUID == currentMessage.senderId) SENT_MESSAGE else RECEIVED_MESSAGE
    }

    fun setOnSentMessageClickListener(messageClickListener: MessageClickListener) {
        this.sentMessageClickListener = messageClickListener
    }

    fun setOnReceivedMessageClickListener(messageClickListener: MessageClickListener) {
        this.receivedMessageClickListener = messageClickListener
    }

    interface MessageClickListener {
        fun onClick(view: View, position: Int)
    }
}