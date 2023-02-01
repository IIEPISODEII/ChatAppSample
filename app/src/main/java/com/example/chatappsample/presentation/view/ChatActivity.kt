package com.example.chatappsample.presentation.view

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.*
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatappsample.R
import com.example.chatappsample.databinding.ActivityChatBinding
import com.example.chatappsample.domain.`interface`.FileDownloadListener
import com.example.chatappsample.domain.`interface`.FileUploadListener
import com.example.chatappsample.domain.dto.MessageDomain
import com.example.chatappsample.presentation.view.adapter.MessageAdapter
import com.example.chatappsample.presentation.viewmodel.ChatViewModel
import com.example.chatappsample.presentation.viewmodel.UserViewModel
import com.example.chatappsample.util.COERCE_DATE_FORMAT
import com.example.chatappsample.util.FULL_DATE_FORMAT
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.stateIn
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
    private val progressBar by lazy { this.findViewById<CircularProgressIndicator>(R.id.prgbar_chat_progressbar) }
    private val imm by lazy { messageBox.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager }

    private lateinit var messageAdapter: MessageAdapter
    private var messageText = ""
    private var isLoading = false

    private var yourId = ""
    private var chatRoomId = ""

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_chat)
        mBinding.viewmodel = chatViewModel
        mBinding.lifecycleOwner = this

        // 액션바 설정
        setSupportActionBar(toolbar)
        toolbarTitleTextView.text = intent.getStringExtra(YOUR_NAME)
        toolbarHomeButton.setOnClickListener { finish() }
        toolbarMenuButton.setOnClickListener { openDrawer() }
        yourId = intent.getStringExtra(YOUR_ID) ?: ""
        chatRoomId = intent.getStringExtra(CHATROOM_ID)!!

        // 뷰모델 기본 리스너 설정
        chatViewModel.run {
            messageTxt.observe(this@ChatActivity) { messageText = it }
            downloadProfileImage(yourId, object: FileDownloadListener {
                override fun onSuccess(byteArray: ByteArray) {
                    messageAdapter.setImageProfileUri(byteArray)
                }

                override fun onFail(e: Exception) {
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
                    val sdf = SimpleDateFormat(FULL_DATE_FORMAT, Locale.KOREA).format(Date(System.currentTimeMillis()))

                    if (clipDatas == null) {
                        val selectedImageUri = it?.data?.data!!
                        val messageDomain = MessageDomain(
                            messageId = sdf+UUID.randomUUID().toString(),
                            messageType = MessageDomain.TYPE_IMAGE,
                            message = selectedImageUri.toString(),
                            senderId = UserViewModel.currentUserId(),
                            sentTime = sdf
                        )
                        lifecycleScope.launch(Dispatchers.IO) {
                            chatViewModel.uploadFile(messageDomain, chatRoomId, onImageSendListener)
                        }
                    } else {
                        clipDatas.let { clipData ->
                            lifecycleScope.launch(Dispatchers.IO) {
                                for (i in 0 until clipDataSize!!) {
                                    val selectedImageUri = clipData.getItemAt(i).uri
                                    val messageDomain = MessageDomain(
                                        messageId = sdf+UUID.randomUUID().toString(),
                                        messageType = MessageDomain.TYPE_IMAGE,
                                        message = selectedImageUri.toString(),
                                        senderId = UserViewModel.currentUserId(),
                                        sentTime = sdf
                                    )
                                    chatViewModel.uploadFile(
                                        messageDomain,
                                        chatRoomId,
                                        onImageSendListener
                                    )
                                }
                            }
                        }
                    }
                }
                hideProgressBar()
            }

        lifecycleScope.launch(Dispatchers.IO) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatViewModel.fetchReaderLogFromRemoteDB(chatRoomId, this)
                chatViewModel.fetchReaderLogAsFlow().stateIn(this).collect {
                    withContext(Dispatchers.Main) { messageAdapter.setReaderLog(it) }
                }
            }
        }

        // 리사이클러뷰에 데이터 추가
        messageAdapter = MessageAdapter(messageDomainList = listOf(), senderUID = UserViewModel.currentUserId())
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
        chatViewModel.messageDomainList.observe(this) { messageList ->
            if (messageList.isEmpty()) return@observe

            messageAdapter.setUriListSize(messageList.size)
            messageAdapter.submitList(messageList.toMutableList())

            // 메시지 처음 받아올 때, 가장 최근 메시지로 스크롤 이동
            if (isFirstLoading) {
                chattingRecyclerView.scrollToPosition(messageAdapter.messageDomainList.lastIndex)
                isFirstLoading = false
            }

            if (chatViewModel.getPreMessageList().isEmpty() || chatViewModel.getPreMessageList().last().messageId == messageList.last().messageId) { // 오래전 메시지 로딩 시

                for (i in 0..messageList.size-chatViewModel.getPreMessageList().size) {
                    if (i !in messageList.indices) break

                    val currMessage = messageList[i]
                    if (currMessage.messageType == MessageDomain.TYPE_NORMAL_TEXT) continue
                    chatViewModel.downloadFile(
                        currMessage,
                        object : FileDownloadListener {
                            override fun onSuccess(byteArray: ByteArray) {
                                messageAdapter.addImageUriToList(i, byteArray)
                                messageAdapter.notifyItemChanged(i)
                            }

                            override fun onFail(e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    )
                }
            } else { // 신규 메시지 추가 시
                val currMessage = messageList.last()
                if (currMessage.messageType != MessageDomain.TYPE_NORMAL_TEXT) {
                    chatViewModel.downloadFile(
                        currMessage,
                        object : FileDownloadListener {
                            override fun onSuccess(byteArray: ByteArray) {
                                messageAdapter.addImageUriToList(messageList.lastIndex, byteArray)
                                messageAdapter.notifyItemChanged(messageList.lastIndex)
                            }

                            override fun onFail(e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    )
                }

                if (currMessage.senderId == UserViewModel.currentUserId()) {
                    lifecycleScope.launch {
                        delay(50L)
                        chattingRecyclerView.layoutManager?.scrollToPosition(messageAdapter.messageDomainList.lastIndex)
                    }
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
                FULL_DATE_FORMAT,
                Locale.KOREA
            ).format(Date(System.currentTimeMillis()))

            val messageDomainObject = MessageDomain(
                messageId = sdf+UUID.randomUUID().toString(),
                messageType = MessageDomain.TYPE_NORMAL_TEXT,
                message = messageText,
                senderId = UserViewModel.currentUserId(),
                sentTime = sdf
            )

            chatViewModel.sendMessage(
                message = messageDomainObject,
                chatRoom = chatRoomId,
                fileUploadListener = onMessageSendListener
            )

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
                        chatViewModel.fetchMessagesFromLocalDB(chatRoom = chatRoomId)
                        isLoading = false
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        chatViewModel.updateChatRoom(
            yourId,
            SimpleDateFormat(
                COERCE_DATE_FORMAT,
                Locale.KOREA
            ).format(Date(System.currentTimeMillis())),
            {},
            {},
            true
        )
    }

    override fun onStop() {
        super.onStop()

        chatViewModel.updateChatRoom(
            yourId,
            SimpleDateFormat(
                COERCE_DATE_FORMAT,
                Locale.KOREA
            ).format(Date(System.currentTimeMillis())),
            {},
            {},
            false
        )
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

    private val onMessageSendListener = object: FileUploadListener {
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

    private val onImageSendListener = object: FileUploadListener {
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
        val animation = AnimationUtils.loadAnimation(this@ChatActivity, R.anim.rotate_progress_indicator)
        progressBar.visibility = View.VISIBLE
        progressBar.startAnimation(animation)
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.GONE
        progressBar.clearAnimation()
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun openDrawer() {
        hideIME()
        drawerDrawerLayout.openDrawer(Gravity.RIGHT)
    }

    companion object {
        const val YOUR_NAME = "selected_name"
        const val YOUR_ID = "selected_uid"
        const val CURRENT_UID = "current_uid"
        const val CHATROOM_ID = "chatroom_id"
    }
}