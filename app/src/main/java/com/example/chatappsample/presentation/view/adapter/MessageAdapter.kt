package com.example.chatappsample.presentation.view.adapter

import android.net.Uri
import android.view.*
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.chatappsample.R
import com.example.chatappsample.domain.dto.Message
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView

class MessageAdapter(var messageList: List<Message>, val senderUID: String) :
    ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffUtil()) {

    // 리사이클러 뷰홀더 타입 지정
    private val EMPTY_MESSAGE = -1
    private val SENT_MESSAGE = 0
    private val RECEIVED_MESSAGE = 1
    private val SENT_IMAGE = 2
    private val RECEIVED_IMAGE = 3

    private var onSentMessageClickListener: OnMessageClickListener? = null
    private var onReceivedMessageClickListener: OnMessageClickListener? = null
    private var onSentImageClickListener: OnImageClickListener? = null
    private var onReceivedImageClickListener: OnImageClickListener? = null

    private val imageList: MutableList<Uri?> = mutableListOf()
    private var profileUri: Uri = "".toUri()

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sentMessageTextView: MaterialTextView = itemView.findViewById(R.id.tv_sent_message)
        private val sentTimeTextView: MaterialTextView = itemView.findViewById(R.id.tv_sent_message_time)

        init {
            sentMessageTextView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) onSentMessageClickListener?.onClick(it, adapterPosition)
            }
        }

        fun bind(message: Message) {
            sentMessageTextView.text = message.message
            sentTimeTextView.text = message.sentTime
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val receivedMessageTextView: MaterialTextView = itemView.findViewById(R.id.tv_received_message)
        private val receivedTimeTextView: MaterialTextView = itemView.findViewById(R.id.tv_received_message_time)
        private val receivedMessageUserProfile: ShapeableImageView = itemView.findViewById(R.id.iv_received_message_user_profile)

        init {
            receivedMessageTextView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) onReceivedMessageClickListener?.onClick(it, adapterPosition)
            }
        }

        fun bind(message: Message) {
            receivedMessageTextView.text = message.message
            receivedTimeTextView.text = message.sentTime

            if (profileUri != "".toUri()) Glide.with(itemView.context)
                .load(profileUri)
                .into(receivedMessageUserProfile)
        }
    }

    inner class SentImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sentImageView: ShapeableImageView = itemView.findViewById(R.id.iv_sent_image)
        private val sentTimeTextView: MaterialTextView = itemView.findViewById(R.id.tv_sent_image_time)

        init {
            sentImageView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) onSentImageClickListener?.onClick(it, adapterPosition)
            }
        }

        fun bind(message: Message, imageUri: Uri?) {
            if (imageUri != null) Glide.with(itemView.context)
                .load(imageUri)
                .transform(MultiTransformation(RoundedCorners(10)))
                .placeholder(R.drawable.ic_outline_image_24)
                .error(R.drawable.ic_outline_image_not_supported_24)
                .skipMemoryCache(false)
                .into(sentImageView)
            sentTimeTextView.text = message.sentTime
        }
    }

    inner class ReceivedImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val receivedImageView: ShapeableImageView = itemView.findViewById(R.id.iv_received_image)
        private val receivedTimeTextView: MaterialTextView = itemView.findViewById(R.id.tv_received_image_time)
        private val receivedImageUserProfile: ShapeableImageView = itemView.findViewById(R.id.iv_received_image_user_profile)

        init {
            receivedImageView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) onReceivedImageClickListener?.onClick(it, adapterPosition)
            }
        }

        fun bind(message: Message, imageUri: Uri?) {
            if (imageUri != null) Glide.with(itemView.context)
                .load(imageUri)
                .transform(MultiTransformation(RoundedCorners(10)))
                .placeholder(R.drawable.ic_outline_image_24)
                .error(R.drawable.ic_outline_image_not_supported_24)
                .skipMemoryCache(false)
                .into(receivedImageView)
            receivedTimeTextView.text = message.sentTime

            if (profileUri != "".toUri()) Glide.with(itemView.context)
                .load(profileUri)
                .into(receivedImageUserProfile)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            EMPTY_MESSAGE, SENT_MESSAGE -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.constraintlayout_sent_message, parent, false)
                SentMessageViewHolder(itemView = itemView)
            }
            SENT_IMAGE -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.constraintlayout_sent_image, parent, false)
                SentImageViewHolder(itemView = itemView)
            }
            RECEIVED_MESSAGE -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.constraintlayout_received_message, parent, false)
                ReceivedMessageViewHolder(itemView = itemView)
            }
            else -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.constraintlayout_received_image, parent, false)
                ReceivedImageViewHolder(itemView = itemView)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (messageList.isEmpty()) return
        when (holder.javaClass) {
            SentMessageViewHolder::class.java -> (holder as SentMessageViewHolder).bind(messageList[position])
            SentImageViewHolder::class.java -> (holder as SentImageViewHolder).bind(messageList[position], imageList[position])
            ReceivedMessageViewHolder::class.java -> (holder as ReceivedMessageViewHolder).bind(messageList[position])
            ReceivedImageViewHolder::class.java -> (holder as ReceivedImageViewHolder).bind(messageList[position], imageList[position])
        }
    }

    fun addImageUriToList(index: Int, uri: Uri) {
        this.imageList[index] = uri
    }

    fun setUriListSize(size: Int) {
        this.imageList.clear()
        repeat(size) {
            this.imageList.add(null)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (messageList.isEmpty()) return EMPTY_MESSAGE
        val currentMessage = messageList[position]

        return when (senderUID) {
            currentMessage.senderId -> {
                if (messageList[position].imageUri != "") SENT_IMAGE
                else SENT_MESSAGE
            }
            else -> {
                if (messageList[position].imageUri != "") RECEIVED_IMAGE
                else RECEIVED_MESSAGE
            }
        }
    }

    fun setOnSentMessageClickListener(onMessageClickListener: OnMessageClickListener) {
        this.onSentMessageClickListener = onMessageClickListener
    }

    fun setOnReceivedMessageClickListener(onMessageClickListener: OnMessageClickListener) {
        this.onReceivedMessageClickListener = onMessageClickListener
    }

    fun setOnSentImageClickListener(onImageClickListener: OnImageClickListener) {
        this.onSentImageClickListener = onImageClickListener
    }

    fun setOnReceivedImageClickListener(onImageClickListener: OnImageClickListener) {
        this.onReceivedImageClickListener = onImageClickListener
    }

    interface OnMessageClickListener {
        fun onClick(view: View, position: Int)
    }

    interface OnImageClickListener {
        fun onClick(view: View, position: Int)
    }

    class MessageDiffUtil : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.messageIndex == newItem.messageIndex
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }

    override fun submitList(list: MutableList<Message>?) {
        super.submitList(list)
        if (list != messageList) messageList = list!!.toList()
    }

    fun setImageProfileUri(uri: Uri) {
        this.profileUri = uri
        notifyDataSetChanged()
    }
}