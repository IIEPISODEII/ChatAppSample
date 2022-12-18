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
import com.example.chatappsample.R
import com.example.chatappsample.databinding.FragmentChatroomListBinding
import com.example.chatappsample.domain.dto.ChatRoomDomain
import com.example.chatappsample.domain.dto.UserDomain
import com.example.chatappsample.presentation.view.adapter.MainChatroomAdapter
import com.example.chatappsample.presentation.viewmodel.ChatViewModel
import com.example.chatappsample.presentation.viewmodel.UserViewModel
import com.example.chatappsample.util.COERCE_DATE_FORMAT
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList
import java.text.SimpleDateFormat

class ChatRoomListFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }
    private lateinit var binding: FragmentChatroomListBinding
    private var chatroomDomainList = arrayListOf<ChatRoomDomain>()
    private var currentUserId = ""
    private lateinit var rvAdapter: MainChatroomAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_chatroom_list, container, false)
        binding.lifecycleOwner = this.viewLifecycleOwner
        binding.viewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Initialize recyclerview
        rvAdapter = MainChatroomAdapter(currentUserId = currentUserId, chatroomDomainList = chatroomDomainList)
        binding.viewModel!!.currentUserDomain.observe(viewLifecycleOwner) {
            currentUserId = it?.uid ?: ""
            rvAdapter.currentUserId = currentUserId
        }
        binding.rvMainChatroomRecyclerview.adapter = rvAdapter

        binding.viewModel!!.chatRoomList.observe(viewLifecycleOwner) { it ->
            val chatroomList = it as ArrayList<ChatRoomDomain>
            rvAdapter.chatroomDomainList = chatroomList
            rvAdapter.chatroomDomainList.forEach { chatroom ->
                lifecycleScope.launch {
                    viewModel.fetchLastMessage(chatroom)
                }
            }
        }
        binding.viewModel!!.userList.observe(viewLifecycleOwner) { it ->
            val userDomainList = it as ArrayList<UserDomain>
            println("유저 리스트: $userDomainList")
//            lifecycleScope.launch {
//                withContext(Dispatchers.IO) {
//                    viewModel.getChatRoomIdsByParticipantsId(currentUserId, userDomainList.map { userDomain -> userDomain.uid})
//                }
//            }
            rvAdapter.chatroomDomainList = userDomainList
            rvAdapter.chatroomDomainList.forEachIndexed { index, user ->
                lifecycleScope.launch {
//                    viewModel.takeLastMessage(user).collect {
//                        println("마지막 메시지: $message")
//                        rvAdapter.addLastMessageToList(user, message)
//                        rvAdapter.notifyItemChanged(index)
//                    }
                }
                rvAdapter.notifyDataSetChanged()
            }

        }

        rvAdapter.setOnChatRoomClickListener(object : MainChatroomAdapter.ChatRoomClickListener {
            override fun onChatRoomClick(view: View, position: Int) {
                showProgressbar()
                val yourInfo = rvAdapter.chatroomDomainList[position]
                val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                    putExtra(ChatActivity.YOUR_NAME, yourInfo.name)
                    putExtra(ChatActivity.YOUR_ID, yourInfo.uid)
                    putExtra(ChatActivity.CURRENT_UID, currentUserId)
                }
                viewModel.updateChatRoom(
                    currentUserId,
                    yourInfo.uid,
                    SimpleDateFormat(
                        COERCE_DATE_FORMAT,
                        Locale.KOREA
                    ).format(Date(System.currentTimeMillis())),
                    {
                        ChatViewModel.setReceiverRoom(it)
                        intent.putExtra(ChatActivity.CHATROOM_ID, it)
                        hideProgressbar()
                        requireContext().startActivity(intent)
                    },
                    {
                        Log.e(
                            this@ChatRoomListFragment::class.java.name,
                            "Fail on updating chatroom"
                        )
                        hideProgressbar()
                    },
                    true
                )
            }
        })
        binding.rvMainChatroomRecyclerview.layoutManager = LinearLayoutManager(requireContext())

        super.onViewCreated(view, savedInstanceState)
    }

    fun showProgressbar() {
        binding.rvMainChatroomRecyclerview.isEnabled = false
        binding.rvMainChatroomRecyclerviewProgressbar.visibility = View.VISIBLE
        binding.rvMainChatroomRecyclerviewProgressbar.show()
    }

    fun hideProgressbar() {
        binding.rvMainChatroomRecyclerview.isEnabled = true
        binding.rvMainChatroomRecyclerviewProgressbar.visibility = View.INVISIBLE
    }
}