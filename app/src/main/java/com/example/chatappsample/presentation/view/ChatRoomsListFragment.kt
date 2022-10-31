package com.example.chatappsample.presentation.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chatappsample.R
import com.example.chatappsample.databinding.FragmentChattingListBinding
import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
import com.example.chatappsample.domain.`interface`.OnGetDataListener
import com.example.chatappsample.domain.dto.Message
import com.example.chatappsample.domain.dto.User
import com.example.chatappsample.presentation.view.adapter.MainUserAdapter
import com.example.chatappsample.presentation.viewmodel.ChatViewModel
import com.example.chatappsample.presentation.viewmodel.UserViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

class ChatRoomsListFragment: Fragment() {

    private val viewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }
    private lateinit var binding: FragmentChattingListBinding
    private var userList = arrayListOf<User>()
    private var currentUserId = ""
    private lateinit var rvAdapter: MainUserAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chatting_list, container, false)
        binding.lifecycleOwner = this.viewLifecycleOwner
        binding.viewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Initialize recyclerview
        rvAdapter = MainUserAdapter(currentUserId = currentUserId, userList = userList)
        binding.viewModel!!.getCurrentUserInformation()
        binding.viewModel!!.currentUser.observe(viewLifecycleOwner) {
            currentUserId = it?.uid ?: ""
            rvAdapter.currentUserId = currentUserId
        }
        binding.rvMainUserRecyclerview.adapter = rvAdapter

        binding.viewModel!!.receiveAllUsersFromExternalDB()

        binding.viewModel!!.allUsersList.observe(viewLifecycleOwner) {
            val userList = it as ArrayList<User>
            rvAdapter.userList = userList
            rvAdapter.userList.forEachIndexed { index, user ->
                lifecycleScope.launch {
                    viewModel.takeLastMessage(user).collect { message ->
                        rvAdapter.addLastMessageToList(user, message)
                        rvAdapter.notifyItemChanged(index)
                    }
                }
                if (user.profileImage.isNotEmpty()) {
                    viewModel.downloadProfileImage(user, object: OnFileDownloadListener {
                        override fun onSuccess(byteArray: ByteArray) {
                            rvAdapter.addProfileImageByteArrayToList(user, byteArray)
                            rvAdapter.notifyItemChanged(index)
                        }

                        override fun onFailure(e: Exception) {
                            e.printStackTrace()
                        }
                    })
                }
                rvAdapter.notifyDataSetChanged()
            }
        }

        rvAdapter.setOnChatRoomClickListener(object: MainUserAdapter.ChatRoomClickListener {
            override fun onChatRoomClick(view: View, position: Int) {
                val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                    putExtra(ChatActivity.OTHER_NAME, rvAdapter.userList[position].name)
                    putExtra(ChatActivity.OTHER_UID, rvAdapter.userList[position].uid)
                    putExtra(ChatActivity.CURRENT_UID, currentUserId)
                }
                ChatViewModel.setReceiverRoom(currentUserId + rvAdapter.userList[position].uid)

                requireContext().startActivity(intent)
            }
        })
        binding.rvMainUserRecyclerview.layoutManager = LinearLayoutManager(requireContext())

        super.onViewCreated(view, savedInstanceState)
    }
}