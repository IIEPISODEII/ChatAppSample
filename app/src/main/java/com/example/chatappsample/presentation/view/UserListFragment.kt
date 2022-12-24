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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatappsample.R
import com.example.chatappsample.databinding.FragmentUserListBinding
import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
import com.example.chatappsample.domain.dto.UserDomain
import com.example.chatappsample.presentation.view.adapter.MainUserAdapter
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
    private var userList = arrayListOf<UserDomain>()
    private var currentUserId = ""
    private lateinit var rvAdapter: MainUserAdapter

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
        rvAdapter = MainUserAdapter(currentUserId = currentUserId, userList = userList)
        binding.viewModel!!.fetchCurrentUserInformation()
        binding.viewModel!!.currentUserDomain.observe(viewLifecycleOwner) {
            currentUserId = it?.uid ?: ""
            rvAdapter.currentUserId = currentUserId
        }
        binding.rvMainUserRecyclerview.adapter = rvAdapter

        binding.viewModel!!.userList.observe(viewLifecycleOwner) {
            val userDomainList = it as ArrayList<UserDomain>
            rvAdapter.userList = userDomainList
            rvAdapter.userList.forEachIndexed { index, user ->
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

        rvAdapter.setOnChatRoomClickListener(object : MainUserAdapter.ChatRoomClickListener {
            override fun onChatRoomClick(view: View, position: Int) {

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