package com.example.chatappsample.presentation.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.chatappsample.R
import com.example.chatappsample.domain.dto.ChatRoomDomain
import com.example.chatappsample.domain.dto.MessageDomain
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView

class MessageAdapter(var messageDomainList: List<MessageDomain>, val senderUID: String) :
    ListAdapter<MessageDomain, RecyclerView.ViewHolder>(MessageDiffUtil()) {

    // 리사이클러 뷰홀더 타입 지정
    private val EMPTY_MESSAGE = -1
    private val MY_MESSAGE = 0
    private val OTHERS_MESSAGE = 1
    private val MY_IMAGE = 2
    private val OTHERS_IMAGE = 3

    private var onMyMessageClickListener: OnMessageClickListener? = null
    private var onOthersMessageClickListener: OnMessageClickListener? = null
    private var onMyImageClickListener: OnImageClickListener? = null
    private var onReceivedImageClickListener: OnImageClickListener? = null

    private val imageList: MutableList<ByteArray?> = mutableListOf()
    private var profileByteArray: ByteArray? = null
    private val readersLog = mutableListOf<ChatRoomDomain.ReaderLog>()

    inner class MyMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val myDateTextView: MaterialTextView = itemView.findViewById(R.id.tv_current_date_presentation_in_my_message_viewholder)
        private val myMessageTextView: MaterialTextView = itemView.findViewById(R.id.tv_my_message)
        private val myTimeTextView: MaterialTextView = itemView.findViewById(R.id.tv_my_message_time)
        private val myReadersTextView: MaterialTextView = itemView.findViewById(R.id.tv_my_reader_logs_message_viewholder)

        init {
            myMessageTextView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) onMyMessageClickListener?.onClick(it, adapterPosition)
            }
        }

        fun bind(messageDomain: MessageDomain, isDateVisible: Boolean) {
            myDateTextView.text = convertSimpleDateFormatToTime(messageDomain.sentTime)[0]
            myDateTextView.visibility = if (isDateVisible) View.VISIBLE else View.GONE
            myMessageTextView.text = messageDomain.message
            myTimeTextView.text = convertSimpleDateFormatToTime(messageDomain.sentTime)[1]

            val leftsToRead = readersLog.count { it.time.isEmpty() || it.time < messageDomain.sentTime }
            if (leftsToRead == 0) {
                myReadersTextView.visibility = View.INVISIBLE
            } else {
                myReadersTextView.text = leftsToRead.toString()
                myReadersTextView.visibility = View.VISIBLE
            }
        }
    }

    inner class OthersMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val othersDateTextView: MaterialTextView = itemView.findViewById(R.id.tv_current_date_presentation_in_others_message_viewholder)
        private val othersMessageTextView: MaterialTextView = itemView.findViewById(R.id.tv_others_message)
        private val othersTimeTextView: MaterialTextView = itemView.findViewById(R.id.tv_others_message_time)
        private val othersMessageUserProfile: ShapeableImageView = itemView.findViewById(R.id.iv_others_message_user_profile)
        private val othersReadersTextView: MaterialTextView = itemView.findViewById(R.id.tv_others_reader_logs_message_viewholder)

        init {
            othersMessageTextView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) onOthersMessageClickListener?.onClick(it, adapterPosition)
            }
        }

        fun bind(messageDomain: MessageDomain, isDateVisible: Boolean) {
            othersDateTextView.text = convertSimpleDateFormatToTime(messageDomain.sentTime)[0]
            othersDateTextView.visibility = if (isDateVisible) View.VISIBLE else View.GONE
            othersMessageTextView.text = messageDomain.message
            othersTimeTextView.text = convertSimpleDateFormatToTime(messageDomain.sentTime)[1]

            val leftsToRead = readersLog.count { it.time.isEmpty() || it.time < messageDomain.sentTime }
            if (leftsToRead == 0) {
                othersReadersTextView.visibility = View.INVISIBLE
            } else {
                othersReadersTextView.text = leftsToRead.toString()
                othersReadersTextView.visibility = View.VISIBLE
            }

            if (profileByteArray != null) Glide.with(itemView.context)
                .load(profileByteArray)
                .into(othersMessageUserProfile)
        }
    }

    inner class MyImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val myDateTextView: MaterialTextView = itemView.findViewById(R.id.tv_current_date_presentation_in_my_image_viewholder)
        private val myImageView: ShapeableImageView = itemView.findViewById(R.id.iv_my_image)
        private val myTimeTextView: MaterialTextView = itemView.findViewById(R.id.tv_my_image_time)
        private val myReadersTextView: MaterialTextView = itemView.findViewById(R.id.tv_my_reader_logs_image_viewholder)

        init {
            myImageView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) onMyImageClickListener?.onClick(it, adapterPosition)
            }
        }

        fun bind(messageDomain: MessageDomain, isDateVisible: Boolean, imageByteArray: ByteArray?) {
            myDateTextView.text = convertSimpleDateFormatToTime(messageDomain.sentTime)[0]
            myDateTextView.visibility = if (isDateVisible) View.VISIBLE else View.GONE
            if (imageByteArray != null) Glide.with(itemView.context)
                .load(imageByteArray)
                .transform(MultiTransformation(RoundedCorners(10)))
                .placeholder(R.drawable.ic_outline_image_24)
                .error(R.drawable.ic_outline_image_not_supported_24)
                .skipMemoryCache(false)
                .into(myImageView)
            myTimeTextView.text = convertSimpleDateFormatToTime(messageDomain.sentTime)[1]

            val leftsToRead = readersLog.count { it.time.isEmpty() || it.time < messageDomain.sentTime }
            if (leftsToRead == 0) {
                myReadersTextView.visibility = View.INVISIBLE
            } else {
                myReadersTextView.text = leftsToRead.toString()
                myReadersTextView.visibility = View.VISIBLE
            }
        }
    }

    inner class OthersImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val othersDateTextView: MaterialTextView = itemView.findViewById(R.id.tv_current_date_presentation_in_others_image_viewholder)
        private val othersImageView: ShapeableImageView = itemView.findViewById(R.id.iv_others_image)
        private val othersTimeTextView: MaterialTextView = itemView.findViewById(R.id.tv_others_image_time)
        private val othersImageUserProfile: ShapeableImageView = itemView.findViewById(R.id.iv_others_image_user_profile)
        private val othersReadersTextView: MaterialTextView = itemView.findViewById(R.id.tv_others_reader_logs_image_viewholder)

        init {
            othersImageView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) onReceivedImageClickListener?.onClick(it, adapterPosition)
            }
        }

        fun bind(messageDomain: MessageDomain, isDateVisible: Boolean, imageByteArray: ByteArray?) {
            othersDateTextView.text = convertSimpleDateFormatToTime(messageDomain.sentTime)[0]
            othersDateTextView.visibility = if (isDateVisible) View.VISIBLE else View.GONE
            if (imageByteArray != null) Glide.with(itemView.context)
                .load(imageByteArray)
                .transform(MultiTransformation(RoundedCorners(10)))
                .placeholder(R.drawable.ic_outline_image_24)
                .error(R.drawable.ic_outline_image_not_supported_24)
                .skipMemoryCache(false)
                .into(othersImageView)
            othersTimeTextView.text = convertSimpleDateFormatToTime(messageDomain.sentTime)[1]

            val leftsToRead = readersLog.count { it.time.isEmpty() || it.time < messageDomain.sentTime }
            if (leftsToRead == 0) {
                othersReadersTextView.visibility = View.INVISIBLE
            } else {
                othersReadersTextView.text = leftsToRead.toString()
                othersReadersTextView.visibility = View.VISIBLE
            }

            if (profileByteArray != null) Glide.with(itemView.context)
                .load(profileByteArray)
                .into(othersImageUserProfile)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            EMPTY_MESSAGE, MY_MESSAGE -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.constraintlayout_my_message, parent, false)
                MyMessageViewHolder(itemView = itemView)
            }
            MY_IMAGE -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.constraintlayout_my_image, parent, false)
                MyImageViewHolder(itemView = itemView)
            }
            OTHERS_MESSAGE -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.constraintlayout_others_message, parent, false)
                OthersMessageViewHolder(itemView = itemView)
            }
            else -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.constraintlayout_others_image, parent, false)
                OthersImageViewHolder(itemView = itemView)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (messageDomainList.isEmpty()) return
        var isDateVisible = false
        // 이번 메시지 송신날짜와 이전 메시지 송신날짜가 다르면 isDateVisible 활성화
        if (position == 0 || convertSimpleDateFormatToTime(messageDomainList[position].sentTime)[0] != convertSimpleDateFormatToTime(messageDomainList[position-1].sentTime)[0]) isDateVisible = true
        when (holder.javaClass) {
            MyMessageViewHolder::class.java -> (holder as MyMessageViewHolder).bind(messageDomainList[position], isDateVisible)
            MyImageViewHolder::class.java -> (holder as MyImageViewHolder).bind(messageDomainList[position], isDateVisible, imageList[position])
            OthersMessageViewHolder::class.java -> (holder as OthersMessageViewHolder).bind(messageDomainList[position], isDateVisible)
            OthersImageViewHolder::class.java -> (holder as OthersImageViewHolder).bind(messageDomainList[position], isDateVisible, imageList[position])
        }
    }

    fun addImageUriToList(index: Int, imageByteArray: ByteArray) {
        this.imageList[index] = imageByteArray
    }

    fun setUriListSize(size: Int) {
        this.imageList.clear()
        repeat(size) {
            this.imageList.add(null)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (messageDomainList.isEmpty()) return EMPTY_MESSAGE

        val currentMessage = messageDomainList[position]

        return when (senderUID) {
            currentMessage.senderId -> {
                if (currentMessage.messageType == MessageDomain.TYPE_IMAGE) MY_IMAGE
                else MY_MESSAGE
            }
            else -> {
                if (currentMessage.messageType == MessageDomain.TYPE_IMAGE) OTHERS_IMAGE
                else OTHERS_MESSAGE
            }
        }
    }

    fun setOnMyMessageClickListener(onMessageClickListener: OnMessageClickListener) {
        this.onMyMessageClickListener = onMessageClickListener
    }

    fun setOnOthersMessageClickListener(onMessageClickListener: OnMessageClickListener) {
        this.onOthersMessageClickListener = onMessageClickListener
    }

    fun setOnMyImageClickListener(onImageClickListener: OnImageClickListener) {
        this.onMyImageClickListener = onImageClickListener
    }

    fun setOnOthersImageClickListener(onImageClickListener: OnImageClickListener) {
        this.onReceivedImageClickListener = onImageClickListener
    }

    interface OnMessageClickListener {
        fun onClick(view: View, position: Int)
    }

    interface OnImageClickListener {
        fun onClick(view: View, position: Int)
    }

    class MessageDiffUtil : DiffUtil.ItemCallback<MessageDomain>() {
        override fun areItemsTheSame(oldItem: MessageDomain, newItem: MessageDomain): Boolean {
            return oldItem.messageId == newItem.messageId
        }

        override fun areContentsTheSame(oldItem: MessageDomain, newItem: MessageDomain): Boolean {
            return oldItem == newItem
        }
    }

    override fun submitList(list: MutableList<MessageDomain>?) {
        super.submitList(list)
        if (list != messageDomainList) messageDomainList = list!!.toList()
    }

    fun setImageProfileUri(byteArray: ByteArray) {
        this.profileByteArray = byteArray
        notifyDataSetChanged()
    }

    fun setReaderLog(list: List<ChatRoomDomain.ReaderLog>) {
        this.readersLog.clear()
        this.readersLog.addAll(list)
        println("ReadersLog in MessageAdapter: $readersLog")
        notifyDataSetChanged()
    }

    private fun convertSimpleDateFormatToTime(sdf: String): Array<String> {
        val dateToRead = sdf.substring(0, if (sdf.lastIndex >= 10) 10 else sdf.lastIndex).split('-').joinToString(".") { it.toInt().toString() }
        val time = sdf.substring(11, 16).split(':').map { it.toInt().toString() }
        val timeToRead = (if (time[0].toInt() < 12) "오전 " + (if (time[0] != "0") time[0] else "12") else "오후 " + (if (time[0] != "12") (time[0].toInt()-12).toString() else "12")) + ":" + time[1].padStart(2, '0')
        return arrayOf(dateToRead, timeToRead)
    }
}