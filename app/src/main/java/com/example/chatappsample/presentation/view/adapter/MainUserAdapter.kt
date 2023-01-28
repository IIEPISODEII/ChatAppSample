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
                onUserClickListener?.onUserClick(view, adapterPosition)
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
            .load(_userProfileImageList[user.uid])
            .error(R.drawable.ic_baseline_person_24)
            .placeholder(R.drawable.ic_baseline_person_24)
            .centerCrop()
            .into(holder.userProfileImage)
    }

    override fun getItemCount(): Int = userList.size

    private val _userProfileImageList = hashMapOf<String, ByteArray>()
    fun getUserProfileImageList() = _userProfileImageList

    fun addProfileImageByteArrayToList(user: String, byteArray: ByteArray) {
        _userProfileImageList[user] = byteArray
    }

    private var onUserClickListener: UserClickListener? = null

    interface UserClickListener {
        fun onUserClick(view: View, position: Int)
    }

    fun setOnUserClickListener(listener: UserClickListener) {
        this.onUserClickListener = listener
    }
}