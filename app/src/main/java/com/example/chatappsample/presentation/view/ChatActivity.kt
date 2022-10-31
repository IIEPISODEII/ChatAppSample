package com.example.chatappsample.presentation.view

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatappsample.R
import com.example.chatappsample.databinding.ActivityChatBinding
import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
import com.example.chatappsample.domain.`interface`.OnFirebaseCommunicationListener
import com.example.chatappsample.domain.dto.Message
import com.example.chatappsample.presentation.view.adapter.MessageAdapter
import com.example.chatappsample.presentation.viewmodel.ChatViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.lang.Integer.max
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    lateinit var chatViewModel: ChatViewModel
    private lateinit var mBinding: ActivityChatBinding

    private val drawerDrawerLayout by lazy { this.findViewById<DrawerLayout>(R.id.drawer_chat) }
    private val toolbar by lazy { this.findViewById<MaterialToolbar>(R.id.tlb_chat_toolbar) }
    private val toolbarHomeButton by lazy { this.findViewById<ShapeableImageView>(R.id.iv_chat_home) }
    private val toolbarMenuButton by lazy { this.findViewById<ShapeableImageView>(R.id.iv_chat_menu) }
    private val toolbarTitleTextView by lazy { this.findViewById<MaterialTextView>(R.id.tv_chat_toolbar_title) }
    private val chattingRecyclerView by lazy { this.findViewById<RecyclerView>(R.id.rv_chat_recyclerview) }
    private val messageBox by lazy { this.findViewById<TextInputEditText>(R.id.et_chat_messagebox) }
    private val sendMessageButton by lazy { this.findViewById<ShapeableImageView>(R.id.imgbtn_send_message) }
    private val addNewItemButton by lazy { this.findViewById<ShapeableImageView>(R.id.imgbtn_add_new_item) }
    private val progressBar by lazy { this.findViewById<ProgressBar>(R.id.prgbar_chat_progressbar) }
    private val imm by lazy { messageBox.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager }

    private lateinit var messageAdapter: MessageAdapter
    var receiverRoom: String? = null
    var senderRoom: String? = null
    private var messageText = ""
    private var isLoading = false

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_chat)
        mBinding.viewmodel = chatViewModel
        mBinding.lifecycleOwner = this

        // 액션바 설정
        setSupportActionBar(toolbar)
        toolbarTitleTextView.text = intent.getStringExtra(OTHER_NAME)
        toolbarHomeButton.setOnClickListener { finish() }
        toolbarMenuButton.setOnClickListener { openDrawer() }

        val otherID = intent.getStringExtra(OTHER_UID)
        val myID = intent.getStringExtra(CURRENT_UID)
        senderRoom = otherID + myID
        receiverRoom = myID + otherID

        // 뷰모델 기본 리스너 설정
        chatViewModel.run {
            messageTxt.observe(this@ChatActivity) { messageText = it }
            downloadProfileImage(otherID!!, object: OnFileDownloadListener {
                override fun onSuccess(byteArray: ByteArray) {
                    messageAdapter.setImageProfileUri(byteArray)
                }

                override fun onFailure(e: Exception) {
                    e.printStackTrace()
                }
            })
        }

        // 사진 전송
        val getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                showProgressBar()
                sendMessageButton.isEnabled = false

                if (it.resultCode == Activity.RESULT_OK) {
                    val clipDatas = it?.data?.clipData
                    val clipDataSize = clipDatas?.itemCount
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.KOREA).format(Date(System.currentTimeMillis()))

                    if (clipDatas == null) {
                        val selectedImageUri = it?.data?.data!!
                        val message = Message(
                            messageId = sdf+UUID.randomUUID().toString(),
                            messageType = Message.TYPE_IMAGE,
                            message = selectedImageUri.toString(),
                            senderId = myID!!,
                            sentTime = sdf
                        )
                        lifecycleScope.launch(Dispatchers.IO) {
                            chatViewModel.uploadFile(message, senderRoom!!, receiverRoom!!, onImageSendListener)
                        }
                    } else {
                        clipDatas.let { clipData ->
                            lifecycleScope.launch(Dispatchers.IO) {
                                for (i in 0 until clipDataSize!!) {
                                    val selectedImageUri = clipData.getItemAt(i).uri
                                    val message = Message(
                                        messageId = sdf+UUID.randomUUID().toString(),
                                        messageType = Message.TYPE_IMAGE,
                                        message = selectedImageUri.toString(),
                                        senderId = myID!!,
                                        sentTime = sdf
                                    )
                                    chatViewModel.uploadFile(
                                        message,
                                        senderRoom!!,
                                        receiverRoom!!,
                                        onImageSendListener
                                    )
                                }
                            }
                        }
                    }
                }
                hideProgressBar()
            }

        // 리사이클러뷰에 데이터 추가
        messageAdapter = MessageAdapter(messageList = listOf(), senderUID = myID ?: "")
        val llm = LinearLayoutManager(this@ChatActivity)
        chattingRecyclerView.apply {
            adapter = messageAdapter
            layoutManager = llm
            addOnItemTouchListener(object: RecyclerView.OnItemTouchListener {
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    hideIME()
                    return false
                }

                override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

            })
            setItemViewCacheSize(30)
        }

        var isFirstLoading = true

        // 채팅 목록 동기화
        chatViewModel.messageList.observe(this) { messageList ->
            if (messageList.isEmpty()) return@observe

            try {
                messageAdapter.setUriListSize(messageList.size)
                messageAdapter.submitList(messageList.toMutableList())
            } catch(e: Exception) {
                e.printStackTrace()
            }

            // 메시지 처음 받아올 때, 가장 최근 메시지로 스크롤 이동
            if (isFirstLoading) {
                chattingRecyclerView.scrollToPosition(messageAdapter.messageList.lastIndex)
                isFirstLoading = false
            }

            if (chatViewModel.getPreMessageList().isEmpty() || chatViewModel.getPreMessageList().last().messageId == messageList.last().messageId) { // 오래전 메시지 로딩 시

                for (i in 0..messageList.size-chatViewModel.getPreMessageList().size) {
                    if (i !in messageList.indices) break

                    val currMessage = messageList[i]
                    if (currMessage.messageType == Message.TYPE_NORMAL_TEXT) continue
                    chatViewModel.downloadFile(
                        currMessage,
                        object : OnFileDownloadListener {
                            override fun onSuccess(byteArray: ByteArray) {
                                messageAdapter.addImageUriToList(i, byteArray)
                                messageAdapter.notifyItemChanged(i)
                            }

                            override fun onFailure(e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    )
                }
            } else { // 신규 메시지 추가 시
                val currMessage = messageList.last()
                if (currMessage.messageType != Message.TYPE_NORMAL_TEXT) {
                    chatViewModel.downloadFile(
                        currMessage,
                        object : OnFileDownloadListener {
                            override fun onSuccess(byteArray: ByteArray) {
                                messageAdapter.addImageUriToList(messageList.lastIndex, byteArray)
                                messageAdapter.notifyItemChanged(messageList.lastIndex)
                            }

                            override fun onFailure(e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    )
                }

                if (messageList.last().senderId == myID!!) {
                    chattingRecyclerView.layoutManager?.scrollToPosition(messageAdapter.messageList.lastIndex+1)
                }
            }

            chatViewModel.setPreMessageList(messageList)
        }

        // 이미지 추가 버튼 클릭
        addNewItemButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                if (p0 == null) return

                val mIntent = Intent().apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    action = Intent.ACTION_PICK
                }
                getContent.launch(mIntent)
            }
        })

        // 메시지 송신
        sendMessageButton.setOnClickListener {

            if (mBinding.etChatMessagebox.text!!.isEmpty()) return@setOnClickListener
            it.isEnabled = false
            showProgressBar()

            val sdf = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss.SSS",
                Locale.KOREA
            ).format(Date(System.currentTimeMillis()))

            val messageObject = Message(
                messageId = sdf+UUID.randomUUID().toString(),
                messageType = Message.TYPE_NORMAL_TEXT,
                message = messageText,
                senderId = myID ?: "",
                sentTime = sdf
            )

            lifecycleScope.launch(Dispatchers.IO) {
                chatViewModel.sendMessage(
                    message = messageObject,
                    senderChatRoom = senderRoom!!,
                    receiverChatRoom = receiverRoom!!,
                    onFirebaseCommunicationListener = onMessageSendListener
                )
            }
            messageBox.setText("")
        }

        // 보낸 메시지 클릭리스너 설정
        messageAdapter.setOnMyMessageClickListener(object :
            MessageAdapter.OnMessageClickListener {
            @RequiresApi(Build.VERSION_CODES.P)
            override fun onClick(view: View, position: Int) {
                onClickSentMessage(view, position)
            }
        })

        // 받은 메시지 클릭리스너 설정
        messageAdapter.setOnOthersMessageClickListener(object :
            MessageAdapter.OnMessageClickListener {
            @RequiresApi(Build.VERSION_CODES.P)
            override fun onClick(view: View, position: Int) {
                onClickReceivedMessage(view, position)
            }
        })

        // 무한스크롤 & 메시지 동적로드
        chattingRecyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = chattingRecyclerView.layoutManager as LinearLayoutManager

                if (isLoading) return
                if (layoutManager.findFirstCompletelyVisibleItemPosition() <= 1) {
                    isLoading = true
                    lifecycleScope.launch(Dispatchers.IO) {
                        chatViewModel.fetchMessagesFromRoomDB(chatRoom = receiverRoom!!)
                        isLoading = false
                    }
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun onClickReceivedMessage(v: View, pos: Int) {
        val clipBoardManager = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipBoardManager.clearPrimaryClip()

        val clipboardItem = ClipData.newPlainText("Clip from ChatSampleApp", "$pos 메시지: ${(v as AppCompatTextView).text}")
        clipBoardManager.setPrimaryClip(clipboardItem)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun onClickSentMessage(v: View, pos: Int) {
        val clipBoardManager = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipBoardManager.clearPrimaryClip()

        val clipboardItem = ClipData.newPlainText("Clip from ChatSampleApp", "$pos 메시지 ${(v as AppCompatTextView).text}")
        clipBoardManager.setPrimaryClip(clipboardItem)
    }

    // 키보드 숨김
    private fun hideIME() {
        imm.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
    }

    companion object {
        const val OTHER_NAME = "selected_name"
        const val OTHER_UID = "selected_uid"
        const val CURRENT_UID = "current_uid"
    }

    private val onMessageSendListener = object: OnFirebaseCommunicationListener {
        override fun onSuccess() {
            hideProgressBar()
            sendMessageButton.isEnabled = true
        }

        override fun onFailure() {
            hideProgressBar()
            Toast.makeText(this@ChatActivity, "메시지 전송에 실패하였습니다.", Toast.LENGTH_SHORT).show()
            sendMessageButton.isEnabled = true
        }
    }

    private val onImageSendListener = object: OnFirebaseCommunicationListener {
        override fun onSuccess() {
            hideProgressBar()
            sendMessageButton.isEnabled = true
        }

        override fun onFailure() {
            hideProgressBar()
            Toast.makeText(this@ChatActivity, "메시지 전송에 실패하였습니다.", Toast.LENGTH_SHORT).show()
            sendMessageButton.isEnabled = true
        }
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun openDrawer() {
        hideIME()
        drawerDrawerLayout.openDrawer(Gravity.RIGHT)
    }
}