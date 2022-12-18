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
import com.example.chatappsample.databinding.FragmentUserListBinding
import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
import com.example.chatappsample.domain.dto.UserDomain
import com.example.chatappsample.presentation.view.adapter.MainChatroomAdapter
import com.example.chatappsample.presentation.viewmodel.ChatViewModel
import com.example.chatappsample.presentation.viewmodel.UserViewModel
import com.example.chatappsample.util.COERCE_DATE_FORMAT
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList
import java.text.SimpleDateFormat

class UserListFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }
    private lateinit var binding: FragmentUserListBinding
    private var userDomainList = arrayListOf<UserDomain>()
    private var currentUserId = ""
    private lateinit var rvAdapter: MainChatroomAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_user_list, container, false)
        binding.lifecycleOwner = this.viewLifecycleOwner
        binding.viewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Initialize recyclerview
        rvAdapter = MainChatroomAdapter(currentUserId = currentUserId, chatroomDomainList = userDomainList)
        binding.viewModel!!.getCurrentUserInformation()
        binding.viewModel!!.currentUserDomain.observe(viewLifecycleOwner) {
            currentUserId = it?.uid ?: ""
            rvAdapter.currentUserId = currentUserId
        }
        binding.rvMainUserRecyclerview.adapter = rvAdapter


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
                if (user.profileImage.isNotEmpty()) {
                    viewModel.downloadProfileImage(user, object : OnFileDownloadListener {
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
                            this@UserListFragment::class.java.name,
                            "Fail on updating chatroom"
                        )
                        hideProgressbar()
                    },
                    true
                )
            }
        })
        binding.rvMainUserRecyclerview.layoutManager = LinearLayoutManager(requireContext())

        super.onViewCreated(view, savedInstanceState)
    }

    fun showProgressbar() {
        binding.rvMainUserRecyclerview.isEnabled = false
        binding.rvMainUserRecyclerviewProgressbar.visibility = View.VISIBLE
        binding.rvMainUserRecyclerviewProgressbar.show()
    }

    fun hideProgressbar() {
        binding.rvMainUserRecyclerview.isEnabled = true
        binding.rvMainUserRecyclerviewProgressbar.visibility = View.INVISIBLE
    }
}