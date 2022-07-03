package com.example.chatappsample.presentation.view.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatappsample.presentation.view.ChatActivity
import com.example.chatappsample.R
import com.example.chatappsample.domain.dto.User
import com.google.android.material.textview.MaterialTextView

class MainUserAdapter(val ctx: Context, var currentUserId: String, var userList: ArrayList<User>): RecyclerView.Adapter<MainUserAdapter.CustomViewHolder>() {

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName = itemView.findViewById<MaterialTextView>(R.id.tv_main_user_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_user, parent, false)
        return CustomViewHolder(itemView = itemView)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val selectedUser = userList[position]
        holder.userName.text = selectedUser.name
        holder.itemView.setOnClickListener {
            val intent = Intent(ctx, ChatActivity::class.java).apply {
                putExtra(ChatActivity.OTHER_NAME, selectedUser.name)
                putExtra(ChatActivity.OTHER_UID, selectedUser.uid)
                putExtra(ChatActivity.CURRENT_UID, currentUserId)
            }

            ctx.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = userList.size
}