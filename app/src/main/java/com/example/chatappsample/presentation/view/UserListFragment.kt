package com.example.chatappsample.presentation.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chatappsample.R
import com.example.chatappsample.databinding.FragmentUserListBinding
import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
import com.example.chatappsample.domain.dto.UserDomain
import com.example.chatappsample.presentation.view.adapter.MainUserAdapter
import com.example.chatappsample.presentation.viewmodel.ChatViewModel
import com.example.chatappsample.presentation.viewmodel.UserViewModel
import com.example.chatappsample.util.COERCE_DATE_FORMAT
import com.example.chatappsample.util.convertDPtoPX
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import java.lang.NullPointerException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

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
                    viewModel.downloadProfileImage(user.uid, object : OnFileDownloadListener {
                        override fun onSuccess(byteArray: ByteArray) {
                            rvAdapter.addProfileImageByteArrayToList(user.uid, byteArray)
                            rvAdapter.notifyItemChanged(index)
                        }

                        override fun onFail(e: Exception) {
                            e.printStackTrace()
                        }
                    })
                }
                rvAdapter.notifyDataSetChanged()
            }

        }

        val bottomSheetView = layoutInflater.inflate(R.layout.dialog_main_user_click, null)
        val bottomSheetDialog = BottomSheetDialog(this@UserListFragment.requireActivity())
        bottomSheetDialog.behavior.expandedOffset = 30

        val dialogProfileImageView = bottomSheetView.findViewById<ShapeableImageView>(R.id.iv_dialog_main_user_profile_image)
        val dialogNameTextView = bottomSheetView.findViewById<MaterialTextView>(R.id.tv_dialog_main_user_profile_name)
        val dialogChatButton = bottomSheetView.findViewById<MaterialCardView>(R.id.cv_dialog_main_user_chat)

        rvAdapter.setOnUserClickListener(object : MainUserAdapter.UserClickListener {
            override fun onUserClick(view: View, position: Int) {

                val yourId = rvAdapter.userList[position].uid
                Glide.with(this@UserListFragment)
                    .load(rvAdapter.getUserProfileImageList()[yourId])
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .error(R.drawable.ic_baseline_person_24)
                    .into(dialogProfileImageView)

                dialogNameTextView.text = rvAdapter.userList[position].name
                dialogChatButton.setOnClickListener {
                    viewModel.updateChatRoom(
                        currentUserId,
                        yourId,
                        SimpleDateFormat(
                            COERCE_DATE_FORMAT,
                            Locale.KOREA
                        ).format(Date(System.currentTimeMillis())),
                        onSuccess = {
                            val chatIntent = Intent(requireContext(), ChatActivity::class.java).apply {
                                putExtra(ChatActivity.YOUR_ID, yourId)
                                putExtra(ChatActivity.CURRENT_UID, viewModel.currentUser())
                            }
                            bottomSheetDialog.dismiss()
                            ChatViewModel.setReceiverRoom(it)
                            chatIntent.putExtra(ChatActivity.CHATROOM_ID, it)
                            if (context == null) {
                                throw NullPointerException("Fragment is not attached on context.")
                            }
                            requireContext().startActivity(chatIntent)
                        },
                        onFail = {},
                        enter = true
                    )
                }

                bottomSheetDialog.setContentView(bottomSheetView)
                bottomSheetDialog.show()
            }
        })
        binding.rvMainUserRecyclerview.layoutManager = LinearLayoutManager(requireContext())

        super.onViewCreated(view, savedInstanceState)
    }
}