package com.example.chatappsample.presentation.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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
import kotlinx.coroutines.flow.stateIn
import java.lang.NullPointerException
import java.util.*
import kotlin.collections.ArrayList
import java.text.SimpleDateFormat

class ChatroomListFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }
    private var binding: FragmentChatroomListBinding? = null
    private var chatroomDomainList = arrayListOf<ChatroomDomain>()
    private lateinit var rvAdapter: MainChatroomAdapter
    private val progressBar by lazy { binding?.rvMainChatroomRecyclerviewProgressbar }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_chatroom_list, container, false)
        binding!!.lifecycleOwner = this.viewLifecycleOwner
        binding!!.viewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        return binding!!.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Initialize recyclerview
        rvAdapter = MainChatroomAdapter(
            currentUserId = UserViewModel.currentUserId(),
            chatroomDomainList = chatroomDomainList
        )
        binding!!.rvMainChatroomRecyclerview.adapter = rvAdapter

        lifecycleScope.launch(Dispatchers.IO) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fetchChatroomList().stateIn(this).collectLatest {
                    if (it.isEmpty()) return@collectLatest

                    val chatroomList = it as ArrayList<ChatroomDomain>
                    rvAdapter.chatroomDomainList = chatroomList
                    rvAdapter.chatroomDomainList.forEachIndexed { idx, chatroom ->
                        viewModel.fetchMessagesFromRemoteDB(chatroom.chatroomId, this)
                        viewModel.fetchLastMessage(chatroom).collectLatest { message ->

                            withContext(Dispatchers.Main) {
                                rvAdapter.addLastMessageToList(chatroom, message)
                                rvAdapter.notifyItemChanged(idx)
                            }
                        }
                    }
                    rvAdapter.notifyDataSetChanged()
                }
            }
        }

        rvAdapter.setOnChatRoomClickListener(object : MainChatroomAdapter.ChatRoomClickListener {
            override fun onChatRoomClick(view: View, position: Int) {
                showProgressbar()
                val chatroom = rvAdapter.chatroomDomainList[position]
                val yourId = chatroom.readerLog.filter { it.userId != UserViewModel.currentUserId() }[0].userId
                val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                    putExtra(ChatActivity.YOUR_ID, yourId)
                    putExtra(ChatActivity.CURRENT_UID, UserViewModel.currentUserId())
                }
                viewModel.updateChatRoom(
                    yourId,
                    SimpleDateFormat(
                        COERCE_DATE_FORMAT,
                        Locale.KOREA
                    ).format(Date(System.currentTimeMillis())),
                    {
                        ChatViewModel.setReceiverRoom(it)
                        intent.putExtra(ChatActivity.CHATROOM_ID, it)
                        hideProgressbar()
                        if (context == null) {
                            throw NullPointerException("Fragment is not attached on context.")
                        }
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
        binding!!.rvMainChatroomRecyclerview.layoutManager = LinearLayoutManager(requireContext())

        super.onViewCreated(view, savedInstanceState)
    }

    fun showProgressbar() {
        binding?.rvMainChatroomRecyclerview?.isEnabled = false
        val rotateAnimation = AnimationUtils.loadAnimation(this.requireActivity(), R.anim.rotate_progress_indicator)
        progressBar?.visibility = View.VISIBLE
        progressBar?.startAnimation(rotateAnimation)
    }

    fun hideProgressbar() {
        binding?.rvMainChatroomRecyclerview?.isEnabled = true
        progressBar?.clearAnimation()
        progressBar?.visibility = View.INVISIBLE
    }
}