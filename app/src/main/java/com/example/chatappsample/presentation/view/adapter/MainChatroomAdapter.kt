package com.example.chatappsample.presentation.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatappsample.R
import com.example.chatappsample.domain.dto.ChatRoomDomain
import com.example.chatappsample.domain.dto.MessageDomain
import com.example.chatappsample.domain.dto.UserDomain
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView

class MainChatroomAdapter(var currentUserId: String, var chatroomDomainList: ArrayList<ChatRoomDomain>): RecyclerView.Adapter<MainChatroomAdapter.CustomViewHolder>() {

    private val lastMessageDomainList: MutableMap<String, MessageDomain> = mutableMapOf()

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userLastMessage: MaterialTextView = itemView.findViewById(R.id.tv_last_chatting)
        val userLastMessageTime: MaterialTextView = itemView.findViewById(R.id.tv_last_chatting_sent_time)

        init {
            itemView.setOnClickListener { view ->
                onChatRoomClickListener?.onChatRoomClick(view, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_chatroom, parent, false)
        return CustomViewHolder(itemView = itemView)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val selectedChatroom = chatroomDomainList[position]
        val lastMessage = lastMessageDomainList[selectedChatroom.chatRoomId]

        holder.userLastMessage.text = lastMessage?.message?.ifEmpty { "사진" } ?: ""
        holder.userLastMessageTime.text = lastMessage?.sentTime ?: ""
    }

    override fun getItemCount(): Int = chatroomDomainList.size

    fun addLastMessageToList(chatRoomDomain: ChatRoomDomain, messageDomain: MessageDomain) {
        lastMessageDomainList[chatRoomDomain.chatRoomId] = messageDomain
    }

    private var onChatRoomClickListener: ChatRoomClickListener? = null

    interface ChatRoomClickListener {
        fun onChatRoomClick(view: View, position: Int)
    }

    fun setOnChatRoomClickListener(listener: ChatRoomClickListener) {
        this.onChatRoomClickListener = listener
    }
}