package com.example.chatappsample.presentation.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatappsample.R
import com.example.chatappsample.domain.dto.UserDomain
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView

class MainUserAdapter(var currentUserId: String, var userList: ArrayList<UserDomain>): RecyclerView.Adapter<MainUserAdapter.CustomViewHolder>() {

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: MaterialTextView = itemView.findViewById(R.id.tv_main_user_name)
        val userProfileImage: ShapeableImageView = itemView.findViewById(R.id.iv_main_user_profile_thumbnail)

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
        val user = userList[position]

        holder.userName.text = user.name
        Glide.with(holder.itemView.context)
            .load(_userProfileImageList[user])
            .into(holder.userProfileImage)
    }

    override fun getItemCount(): Int = userList.size

    private val _userProfileImageList = hashMapOf<UserDomain, ByteArray>()
    fun addProfileImageByteArrayToList(user: UserDomain, byteArray: ByteArray) {
        _userProfileImageList[user] = byteArray
    }

    private var onChatRoomClickListener: ChatRoomClickListener? = null

    interface ChatRoomClickListener {
        fun onChatRoomClick(view: View, position: Int)
    }

    fun setOnChatRoomClickListener(listener: ChatRoomClickListener) {
        this.onChatRoomClickListener = listener
    }
}