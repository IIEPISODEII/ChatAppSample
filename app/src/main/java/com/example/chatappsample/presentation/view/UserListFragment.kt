package com.example.chatappsample.presentation.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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
import com.bumptech.glide.Glide
import com.example.chatappsample.R
import com.example.chatappsample.databinding.FragmentUserListBinding
import com.example.chatappsample.domain.`interface`.FileDownloadListener
import com.example.chatappsample.domain.dto.UserDomain
import com.example.chatappsample.presentation.view.adapter.MainUserAdapter
import com.example.chatappsample.presentation.viewmodel.ChatViewModel
import com.example.chatappsample.presentation.viewmodel.UserViewModel
import com.example.chatappsample.util.COERCE_DATE_FORMAT
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.NullPointerException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class UserListFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }
    private lateinit var binding: FragmentUserListBinding
    private var userList = arrayListOf<UserDomain>()
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
        rvAdapter = MainUserAdapter(currentUserId = UserViewModel.currentUserId(), userList = userList)
        binding.rvMainUserRecyclerview.adapter = rvAdapter

        lifecycleScope.launch(Dispatchers.IO) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fetchAllUsersList().collectLatest { list ->
                    val userDomainList = list.filter { it.uid != UserViewModel.currentUserId() } as ArrayList<UserDomain>
                    rvAdapter.userList = userDomainList
                    rvAdapter.userList.forEachIndexed { index, user ->
                        if (user.profileImage.isNotEmpty()) {
                            viewModel.downloadProfileImage(user.uid, object : FileDownloadListener {
                                override fun onSuccess(byteArray: ByteArray) {
                                    rvAdapter.addProfileImageByteArrayToList(user.uid, byteArray)
                                    rvAdapter.notifyItemChanged(index)
                                }

                                override fun onFail(e: Exception) {
                                    e.printStackTrace()
                                }
                            })
                        }
                        launch(Dispatchers.Main) {
                            rvAdapter.notifyDataSetChanged()
                        }
                    }

                }
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
                    .centerCrop()
                    .into(dialogProfileImageView)

                dialogNameTextView.text = rvAdapter.userList[position].name
                dialogChatButton.setOnClickListener {
                    viewModel.updateChatRoom(
                        yourId,
                        SimpleDateFormat(
                            COERCE_DATE_FORMAT,
                            Locale.KOREA
                        ).format(Date(System.currentTimeMillis())),
                        onSuccess = {
                            val chatIntent = Intent(requireContext(), ChatActivity::class.java).apply {
                                putExtra(ChatActivity.YOUR_ID, yourId)
                                putExtra(ChatActivity.CURRENT_UID, UserViewModel.currentUserId())
                                putExtra(ChatActivity.CHATROOM_ID, it)
                            }
                            bottomSheetDialog.dismiss()
                            ChatViewModel.setReceiverRoom(it)
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