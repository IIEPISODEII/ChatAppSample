package com.example.chatappsample.presentation.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chatappsample.R
import com.example.chatappsample.domain.dto.ChatroomDomain
import com.example.chatappsample.domain.dto.MessageDomain
import com.example.chatappsample.util.convertSimpleDateFormatToTime
import com.google.android.material.textview.MaterialTextView

class MainChatroomAdapter(var currentUserId: String, var chatroomDomainList: List<ChatroomDomain>):
    ListAdapter<ChatroomDomain, MainChatroomAdapter.CustomViewHolder>(ChatroomDiffUtil()) {

    private val chatroomToLastMessageMap: HashMap<String, MessageDomain?> = hashMapOf()
    private val chatroomToNameMap = hashMapOf<String, HashMap<String, String>>()

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chatroomName: MaterialTextView = itemView.findViewById(R.id.tv_main_chatroom_name)
        val userLastMessage: MaterialTextView = itemView.findViewById(R.id.tv_main_chatroom_last_chatting)
        val userLastMessageTime: MaterialTextView = itemView.findViewById(R.id.tv_main_chatroom_last_chatting_sent_time)

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
        val lastMessage = chatroomToLastMessageMap[selectedChatroom.chatroomId]

        holder.chatroomName.text = chatroomToNameMap[selectedChatroom.chatroomId]?.values?.joinToString(separator = ", ", postfix = "님과의 대화방") ?: "님과의 대화방"
        holder.userLastMessage.text = lastMessage?.message?.ifEmpty { "사진" } ?: ""
        holder.userLastMessageTime.text = lastMessage?.sentTime ?: ""

        holder.userLastMessageTime.text = if (lastMessage == null) "" else {
            val currentTime = convertSimpleDateFormatToTime(lastMessage.sentTime)[0]
            currentTime
        }
    }

    override fun getItemCount(): Int = chatroomDomainList.size

    fun addLastMessageToMap(chatRoomDomain: ChatroomDomain, messageDomain: MessageDomain?) {
        chatroomToLastMessageMap[chatRoomDomain.chatroomId] = messageDomain
    }

    /**
     * Set ChatroomName
     *
     * @param chatroomDomain
     * @param chatroomName2
     * @return return index of modified chatroom position of adapter list
     */
    fun addChatroomNameToMap(chatroomDomain: ChatroomDomain, uid: String, userName: String): Int {
        if (chatroomDomain.chatroomId !in chatroomToNameMap) chatroomToNameMap[chatroomDomain.chatroomId] = hashMapOf()
        chatroomToNameMap[chatroomDomain.chatroomId]?.put(uid, userName)

        return chatroomDomainList.indexOfFirst { it.chatroomId == chatroomDomain.chatroomId }
    }

    fun getLastMessageToMap() = chatroomToLastMessageMap

    private var onChatRoomClickListener: ChatRoomClickListener? = null

    interface ChatRoomClickListener {
        fun onChatRoomClick(view: View, position: Int)
    }

    fun setOnChatRoomClickListener(listener: ChatRoomClickListener) {
        this.onChatRoomClickListener = listener
    }

    class ChatroomDiffUtil : DiffUtil.ItemCallback<ChatroomDomain>() {
        override fun areItemsTheSame(oldItem: ChatroomDomain, newItem: ChatroomDomain): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: ChatroomDomain, newItem: ChatroomDomain): Boolean {
            return false
        }
    }
}