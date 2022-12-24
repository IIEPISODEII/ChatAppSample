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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatappsample.R
import com.example.chatappsample.databinding.FragmentChatroomListBinding
import com.example.chatappsample.domain.dto.ChatroomDomain
import com.example.chatappsample.presentation.view.adapter.MainChatroomAdapter
import com.example.chatappsample.presentation.viewmodel.ChatViewModel
import com.example.chatappsample.presentation.viewmodel.UserViewModel
import com.example.chatappsample.util.COERCE_DATE_FORMAT
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import java.util.*
import kotlin.collections.ArrayList
import java.text.SimpleDateFormat

class ChatroomListFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }
    private lateinit var binding: FragmentChatroomListBinding
    private var chatroomDomainList = arrayListOf<ChatroomDomain>()
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
        rvAdapter = MainChatroomAdapter(
            currentUserId = currentUserId,
            chatroomDomainList = chatroomDomainList
        )
        binding.viewModel!!.currentUserDomain.observe(viewLifecycleOwner) {
            currentUserId = it?.uid ?: ""
            rvAdapter.currentUserId = currentUserId
        }
        binding.rvMainChatroomRecyclerview.adapter = rvAdapter

        lifecycleScope.launch(Dispatchers.IO) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                binding.viewModel!!.chatroomList.collectLatest {
                    if (it.isEmpty()) return@collectLatest
                    println("┏━채팅방 목록━━━━")
                    it.forEach { c ->
                        println("┃$c")
                    }
                    println("┗━━━━━━━━━━━━━━")

                    val chatroomList = it as ArrayList<ChatroomDomain>
                    withContext(Dispatchers.Main) {
                        rvAdapter.chatroomDomainList = chatroomList
                        rvAdapter.chatroomDomainList.forEachIndexed { idx, chatroom ->
                            viewModel.fetchLastMessage(chatroom).collect { message ->
                                rvAdapter.addLastMessageToList(chatroom, message)
                                rvAdapter.notifyItemChanged(idx)
                            }
                        }
                        rvAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
//        binding.viewModel!!.userList.observe(viewLifecycleOwner) { it ->
//            val userDomainList = it as ArrayList<UserDomain>
//            lifecycleScope.launch {
//                withContext(Dispatchers.IO) {
//                    viewModel.getChatRoomIdsByParticipantsId(currentUserId, userDomainList.map { userDomain -> userDomain.uid})
//                }
//            }
//            rvAdapter.chatroomDomainList = userDomainList
//            rvAdapter.chatroomDomainList.forEachIndexed { index, user ->
//                lifecycleScope.launch {
//                    viewModel.takeLastMessage(user).collect {
//                        println("마지막 메시지: $message")
//                        rvAdapter.addLastMessageToList(user, message)
//                        rvAdapter.notifyItemChanged(index)
//                    }
//                }
//                rvAdapter.notifyDataSetChanged()
//            }
//
//        }

        rvAdapter.setOnChatRoomClickListener(object : MainChatroomAdapter.ChatRoomClickListener {
            override fun onChatRoomClick(view: View, position: Int) {
                showProgressbar()
                val chatroom = rvAdapter.chatroomDomainList[position]
                val yourId = chatroom.readerLog.filter { it.userId != currentUserId }[0].userId
                val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                    putExtra(ChatActivity.YOUR_ID, yourId)
                    putExtra(ChatActivity.CURRENT_UID, currentUserId)
                }
                viewModel.updateChatRoom(
                    currentUserId,
                    yourId,
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
                            this@ChatroomListFragment::class.java.name,
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