package com.example.chatappsample.presentation.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatappsample.R
import com.example.chatappsample.domain.dto.MessageDomain
import com.example.chatappsample.domain.dto.UserDomain
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView

class MainUserAdapter(var currentUserId: String, var userDomainList: ArrayList<UserDomain>): RecyclerView.Adapter<MainUserAdapter.CustomViewHolder>() {

    private val lastMessageDomainList: MutableMap<String, MessageDomain> = mutableMapOf()
    private val profileImageByteList: MutableMap<String, ByteArray> = mutableMapOf()

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName = itemView.findViewById<MaterialTextView>(R.id.tv_main_user_name)
        val userLastMessage = itemView.findViewById<MaterialTextView>(R.id.tv_last_chatting)
        val userLastMessageTime = itemView.findViewById<MaterialTextView>(R.id.tv_last_chatting_sent_time)
        val userProfileImage = itemView.findViewById<ShapeableImageView>(R.id.iv_main_user_profile_thumbnail)

        init {
            itemView.setOnClickListener { view ->
                onChatRoomClickListener?.onChatRoomClick(view, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_user, parent, false)
        return CustomViewHolder(itemView = itemView)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val selectedUser = userDomainList[position]
        holder.userName.text = selectedUser.name
        holder.userLastMessage.text = if (selectedUser.profileImage.isNotEmpty()) "사진" else lastMessageDomainList[selectedUser.uid]?.message
        holder.userLastMessageTime.text = lastMessageDomainList[selectedUser.uid]?.sentTime
        Glide
            .with(holder.itemView.context)
            .load(profileImageByteList[selectedUser.uid])
            .into(holder.userProfileImage)
    }

    override fun getItemCount(): Int = userDomainList.size

    fun addLastMessageToList(userDomain: UserDomain, messageDomain: MessageDomain) {
        lastMessageDomainList[userDomain.uid] = messageDomain
    }

    fun addProfileImageByteArrayToList(userDomain: UserDomain, byteArray: ByteArray) {
        profileImageByteList[userDomain.uid] = byteArray
    }

    private var onChatRoomClickListener: ChatRoomClickListener? = null

    interface ChatRoomClickListener {
        fun onChatRoomClick(view: View, position: Int)
    }

    fun setOnChatRoomClickListener(listener: ChatRoomClickListener) {
        this.onChatRoomClickListener = listener
    }
}