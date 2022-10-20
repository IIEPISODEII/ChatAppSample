package com.example.chatappsample.presentation.view

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
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
    private val partitionConstraintLayout by lazy { this.findViewById<ConstraintLayout>(R.id.drawer_chat_partition) }
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


        chatViewModel.messageTxt.observe(this) { messageText = it }

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

        var originalHeight = -1
        var keyboardHeight = 0

        // 리사이클러뷰 레이아웃 측정(액티비티 onCreate()시에만 설정)
        chattingRecyclerView.apply {
            viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {

                    // 키보드 높이 추출 (액티비티 생성 시 최초 1회만 계산)
                    originalHeight = max(chattingRecyclerView.height, originalHeight)
                    if (originalHeight - chattingRecyclerView.height > 100) {
                        keyboardHeight = originalHeight - chattingRecyclerView.height
                        chattingRecyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }

                    chattingRecyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
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
            setItemViewCacheSize(15)
        }

        // 상대방 프로필 이미지 다운로드
        chatViewModel.downloadProfileImage(otherID!!, object: OnFileDownloadListener {
            override fun onSuccess(byteArray: ByteArray) {
                messageAdapter.setImageProfileUri(byteArray)
            }

            override fun onFailure(e: Exception) {
                e.printStackTrace()
            }
        })

        // 채팅 목록 동기화
        val messageList = ArrayList<Message>()

        lifecycleScope.launch(Dispatchers.Main) {
            val fetchMessagesJob = async(Dispatchers.IO) {
                chatViewModel.fetchMessagesFromExternalDB(receiverRoom!!)
                chatViewModel.fetchMessagesFromRoomDB(receiverRoom!!)
            }

            messageList.addAll(fetchMessagesJob.await())

            chatViewModel.fetchLastMessageFromRoomDB(receiverRoom!!, lifecycleScope).collect {
                chatViewModel.addMessageAfterLaunchingActivity()
                messageList.add(it)
                messageAdapter.setUriListSize(messageList.size)
                messageAdapter.submitList(messageList.toMutableList())

                messageList.forEachIndexed { idx, currMessage ->
                    if (currMessage.messageType == Message.TYPE_NORMAL_TEXT) return@forEachIndexed

                    withContext(Dispatchers.IO) {
                        chatViewModel.downloadFile(
                            currMessage,
                            object : OnFileDownloadListener {
                                override fun onSuccess(byteArray: ByteArray) {
                                    messageAdapter.addImageUriToList(idx, byteArray)
                                    messageAdapter.notifyItemChanged(idx)
                                }

                                override fun onFailure(e: Exception) {
                                    e.printStackTrace()
                                }
                            })
                    }
                }
                if (messageList.last().senderId == myID!!) {
                    chattingRecyclerView.scrollToPosition(messageAdapter.messageList.lastIndex)
                }
            }


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

        // 키보드 생성/파괴 시 채팅 리사이클러뷰 스크롤
        chattingRecyclerView.viewTreeObserver.addOnGlobalLayoutListener {
            println("KEYBOARD HEIGHT : $keyboardHeight\nCHATTING RECYCLERVIEW HEIGHT: ${chattingRecyclerView.height}")
//            if (chattingRecyclerView.height < originalHeight) chattingRecyclerView.scrollBy(0, keyboardHeight / 2)
//            else chattingRecyclerView.scrollBy(0, -keyboardHeight / 2)
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
            sendMessageButton.isEnabled = true
        }

        override fun onFailure() {
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