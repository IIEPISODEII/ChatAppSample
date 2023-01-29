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
import com.example.chatappsample.domain.dto.ChatroomDomain
import com.example.chatappsample.domain.dto.MessageDomain
import com.example.chatappsample.util.convertSimpleDateFormatToTime
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView

class MessageAdapter(var messageDomainList: List<MessageDomain>, val senderUID: String) :
    ListAdapter<MessageDomain, RecyclerView.ViewHolder>(MessageDiffUtil()) {

    // 리사이클러 뷰홀더 타입 지정
    private val EMPTY_MESSAGE_WITH_DATE = -1
    private val MY_MESSAGE_WITH_DATE = 0
    private val OTHERS_MESSAGE_WITH_DATE = 1
    private val MY_IMAGE_WITH_DATE = 2
    private val OTHERS_IMAGE_WITH_DATE = 3
    private val MY_MESSAGE_WITHOUT_DATE = 4
    private val OTHERS_MESSAGE_WITHOUT_DATE = 5
    private val MY_IMAGE_WITHOUT_DATE = 6
    private val OTHERS_IMAGE_WITHOUT_DATE = 7

    private var onMyMessageClickListener: OnMessageClickListener? = null
    private var onOthersMessageClickListener: OnMessageClickListener? = null
    private var onMyImageClickListener: OnImageClickListener? = null
    private var onReceivedImageClickListener: OnImageClickListener? = null

    private val imageList: MutableList<ByteArray?> = mutableListOf()
    private var profileByteArray: ByteArray? = null
    private val readersLog = mutableListOf<ChatroomDomain.ReaderLogDomain>()

    inner class MyMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val myDateTextView: MaterialTextView = itemView.findViewById(R.id.tv_current_date_presentation_in_my_message_viewholder_with_date)
        private val myMessageTextView: MaterialTextView = itemView.findViewById(R.id.tv_my_message_with_date)
        private val myTimeTextView: MaterialTextView = itemView.findViewById(R.id.tv_my_message_time_with_date)
        private val myReadersTextView: MaterialTextView = itemView.findViewById(R.id.tv_my_reader_logs_message_viewholder_with_date)

        init {
            myMessageTextView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) onMyMessageClickListener?.onClick(it, adapterPosition)
            }
        }

        fun bind(messageDomain: MessageDomain) {
            myDateTextView.text = convertSimpleDateFormatToTime(messageDomain.sentTime)[0]
            myMessageTextView.text = messageDomain.message
            myTimeTextView.text = convertSimpleDateFormatToTime(messageDomain.sentTime)[1]

            val messageSentTimeInFormat = messageDomain.sentTime
                .replace(" ", "")
                .replace("-", "")
                .replace(":", "")
                .replace(".", "")
                .dropLast(3)

            val leftsToRead = readersLog.count { it.readTime.isEmpty() || it.readTime < messageSentTimeInFormat }

            if (leftsToRead == 0) {
                myReadersTextView.visibility = View.INVISIBLE
            } else {
                myReadersTextView.text = leftsToRead.toString()
                myReadersTextView.visibility = View.VISIBLE
            }
        }
    }
    inner class MyMessageWithoutDateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val myMessageTextView: MaterialTextView = itemView.findViewById(R.id.tv_my_message_without_date)
        private val myTimeTextView: MaterialTextView = itemView.findViewById(R.id.tv_my_message_time_without_date)
        private val myReadersTextView: MaterialTextView = itemView.findViewById(R.id.tv_my_reader_logs_message_viewholder_without_date)

        init {
            myMessageTextView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) onMyMessageClickListener?.onClick(it, adapterPosition)
            }
        }

        fun bind(messageDomain: MessageDomain) {
            myMessageTextView.text = messageDomain.message
            myTimeTextView.text = convertSimpleDateFormatToTime(messageDomain.sentTime)[1]

            val messageSentTimeInFormat = messageDomain.sentTime
                .replace(" ", "")
                .replace("-", "")
                .replace(":", "")
                .replace(".", "")
                .dropLast(3)

            val leftsToRead = readersLog.count { it.readTime.isEmpty() || it.readTime < messageSentTimeInFormat }

            if (leftsToRead == 0) {
                myReadersTextView.visibility = View.INVISIBLE
            } else {
                myReadersTextView.text = leftsToRead.toString()
                myReadersTextView.visibility = View.VISIBLE
            }
        }
    }

    inner class OthersMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val othersDateTextView: MaterialTextView = itemView.findViewById(R.id.tv_current_date_presentation_in_others_message_viewholder_with_date)
        private val othersMessageTextView: MaterialTextView = itemView.findViewById(R.id.tv_others_message_with_date)
        private val othersTimeTextView: MaterialTextView = itemView.findViewById(R.id.tv_others_message_time_with_date)
        private val othersMessageUserProfile: ShapeableImageView = itemView.findViewById(R.id.iv_others_message_user_profile_with_date)
        private val othersReadersTextView: MaterialTextView = itemView.findViewById(R.id.tv_others_reader_logs_message_viewholder_with_date)

        init {
            othersMessageTextView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) onOthersMessageClickListener?.onClick(it, adapterPosition)
            }
        }

        fun bind(messageDomain: MessageDomain) {
            othersDateTextView.text = convertSimpleDateFormatToTime(messageDomain.sentTime)[0]
            othersMessageTextView.text = messageDomain.message
            othersTimeTextView.text = convertSimpleDateFormatToTime(messageDomain.sentTime)[1]

            val messageSentTimeInFormat = messageDomain.sentTime
                .replace(" ", "")
                .replace("-", "")
                .replace(":", "")
                .replace(".", "")
                .dropLast(3)

            val leftsToRead = readersLog.count { it.readTime.isEmpty() || it.readTime < messageSentTimeInFormat }
            if (leftsToRead == 0) {
                othersReadersTextView.visibility = View.INVISIBLE
            } else {
                othersReadersTextView.text = leftsToRead.toString()
                othersReadersTextView.visibility = View.VISIBLE
            }

            if (profileByteArray != null) Glide.with(itemView.context)
                .load(profileByteArray)
                .centerCrop()
                .into(othersMessageUserProfile)
        }
    }
    inner class OthersMessageWithoutDateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val othersMessageTextView: MaterialTextView = itemView.findViewById(R.id.tv_others_message_without_date)
        private val othersTimeTextView: MaterialTextView = itemView.findViewById(R.id.tv_others_message_time_without_date)
        private val othersMessageUserProfile: ShapeableImageView = itemView.findViewById(R.id.iv_others_message_user_profile_without_date)
        private val othersReadersTextView: MaterialTextView = itemView.findViewById(R.id.tv_others_reader_logs_message_viewholder_without_date)

        init {
            othersMessageTextView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) onOthersMessageClickListener?.onClick(it, adapterPosition)
            }
        }

        fun bind(messageDomain: MessageDomain) {
            othersMessageTextView.text = messageDomain.message
            othersTimeTextView.text = convertSimpleDateFormatToTime(messageDomain.sentTime)[1]

            val messageSentTimeInFormat = messageDomain.sentTime
                .replace(" ", "")
                .replace("-", "")
                .replace(":", "")
                .replace(".", "")
                .dropLast(3)

            val leftsToRead = readersLog.count { it.readTime.isEmpty() || it.readTime < messageSentTimeInFormat }
            if (leftsToRead == 0) {
                othersReadersTextView.visibility = View.INVISIBLE
            } else {
                othersReadersTextView.text = leftsToRead.toString()
                othersReadersTextView.visibility = View.VISIBLE
            }

            if (profileByteArray != null) Glide.with(itemView.context)
                .load(profileByteArray)
                .centerCrop()
                .into(othersMessageUserProfile)
        }
    }

    inner class MyImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val myDateTextView: MaterialTextView = itemView.findViewById(R.id.tv_current_date_presentation_in_my_image_viewholder_with_date)
        private val myImageView: ShapeableImageView = itemView.findViewById(R.id.iv_my_image_with_date)
        private val myTimeTextView: MaterialTextView = itemView.findViewById(R.id.tv_my_image_time_with_date)
        private val myReadersTextView: MaterialTextView = itemView.findViewById(R.id.tv_my_reader_logs_image_viewholder_with_date)

        init {
            myImageView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) onMyImageClickListener?.onClick(it, adapterPosition)
            }
        }

        fun bind(messageDomain: MessageDomain, imageByteArray: ByteArray?) {
            myDateTextView.text = convertSimpleDateFormatToTime(messageDomain.sentTime)[0]
            if (imageByteArray != null) Glide.with(itemView.context)
                .load(imageByteArray)
                .transform(MultiTransformation(RoundedCorners(10)))
                .placeholder(R.drawable.ic_outline_image_24)
                .error(R.drawable.ic_outline_image_not_supported_24)
                .skipMemoryCache(false)
                .into(myImageView)
            myTimeTextView.text = convertSimpleDateFormatToTime(messageDomain.sentTime)[1]

            val messageSentTimeInFormat = messageDomain.sentTime
                .replace(" ", "")
                .replace("-", "")
                .replace(":", "")
                .replace(".", "")
                .dropLast(3)

            val leftsToRead = readersLog.count { it.readTime.isEmpty() || it.readTime < messageSentTimeInFormat }
            if (leftsToRead == 0) {
                myReadersTextView.visibility = View.INVISIBLE
            } else {
                myReadersTextView.text = leftsToRead.toString()
                myReadersTextView.visibility = View.VISIBLE
            }
        }
    }
    inner class MyImageWithoutDateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val myImageView: ShapeableImageView = itemView.findViewById(R.id.iv_my_image_without_date)
        private val myTimeTextView: MaterialTextView = itemView.findViewById(R.id.tv_my_image_time_without_date)
        private val myReadersTextView: MaterialTextView = itemView.findViewById(R.id.tv_my_reader_logs_image_viewholder_without_date)

        init {
            myImageView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) onMyImageClickListener?.onClick(it, adapterPosition)
            }
        }

        fun bind(messageDomain: MessageDomain, imageByteArray: ByteArray?) {
            if (imageByteArray != null) Glide.with(itemView.context)
                .load(imageByteArray)
                .transform(MultiTransformation(RoundedCorners(10)))
                .placeholder(R.drawable.ic_outline_image_24)
                .error(R.drawable.ic_outline_image_not_supported_24)
                .skipMemoryCache(false)
                .into(myImageView)
            myTimeTextView.text = convertSimpleDateFormatToTime(messageDomain.sentTime)[1]

            val messageSentTimeInFormat = messageDomain.sentTime
                .replace(" ", "")
                .replace("-", "")
                .replace(":", "")
                .replace(".", "")
                .dropLast(3)

            val leftsToRead = readersLog.count { it.readTime.isEmpty() || it.readTime < messageSentTimeInFormat }
            if (leftsToRead == 0) {
                myReadersTextView.visibility = View.INVISIBLE
            } else {
                myReadersTextView.text = leftsToRead.toString()
                myReadersTextView.visibility = View.VISIBLE
            }
        }
    }

    inner class OthersImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val othersDateTextView: MaterialTextView = itemView.findViewById(R.id.tv_current_date_presentation_in_others_image_viewholder_with_date)
        private val othersImageView: ShapeableImageView = itemView.findViewById(R.id.iv_others_image_with_date)
        private val othersTimeTextView: MaterialTextView = itemView.findViewById(R.id.tv_others_image_time_with_date)
        private val othersImageUserProfile: ShapeableImageView = itemView.findViewById(R.id.iv_others_image_user_profile_with_date)
        private val othersReadersTextView: MaterialTextView = itemView.findViewById(R.id.tv_others_reader_logs_image_viewholder_with_date)

        init {
            othersImageView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) onReceivedImageClickListener?.onClick(it, adapterPosition)
            }
        }

        fun bind(messageDomain: MessageDomain, imageByteArray: ByteArray?) {
            othersDateTextView.text = convertSimpleDateFormatToTime(messageDomain.sentTime)[0]
            if (imageByteArray != null) Glide.with(itemView.context)
                .load(imageByteArray)
                .transform(MultiTransformation(RoundedCorners(10)))
                .placeholder(R.drawable.ic_outline_image_24)
                .error(R.drawable.ic_outline_image_not_supported_24)
                .skipMemoryCache(false)
                .into(othersImageView)
            othersTimeTextView.text = convertSimpleDateFormatToTime(messageDomain.sentTime)[1]

            val messageSentTimeInFormat = messageDomain.sentTime
                .replace(" ", "")
                .replace("-", "")
                .replace(":", "")
                .replace(".", "")
                .dropLast(3)

            val leftsToRead = readersLog.count { it.readTime.isEmpty() || it.readTime < messageSentTimeInFormat }
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
    inner class OthersImageWithoutDateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val othersImageView: ShapeableImageView = itemView.findViewById(R.id.iv_others_image_without_date)
        private val othersTimeTextView: MaterialTextView = itemView.findViewById(R.id.tv_others_image_time_without_date)
        private val othersImageUserProfile: ShapeableImageView = itemView.findViewById(R.id.iv_others_image_user_profile_without_date)
        private val othersReadersTextView: MaterialTextView = itemView.findViewById(R.id.tv_others_reader_logs_image_viewholder_without_date)

        init {
            othersImageView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) onReceivedImageClickListener?.onClick(it, adapterPosition)
            }
        }

        fun bind(messageDomain: MessageDomain, imageByteArray: ByteArray?) {
            if (imageByteArray != null) Glide.with(itemView.context)
                .load(imageByteArray)
                .transform(MultiTransformation(RoundedCorners(10)))
                .placeholder(R.drawable.ic_outline_image_24)
                .error(R.drawable.ic_outline_image_not_supported_24)
                .skipMemoryCache(false)
                .into(othersImageView)
            othersTimeTextView.text = convertSimpleDateFormatToTime(messageDomain.sentTime)[1]

            val messageSentTimeInFormat = messageDomain.sentTime
                .replace(" ", "")
                .replace("-", "")
                .replace(":", "")
                .replace(".", "")
                .dropLast(3)

            val leftsToRead = readersLog.count { it.readTime.isEmpty() || it.readTime < messageSentTimeInFormat }
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
            EMPTY_MESSAGE_WITH_DATE, MY_MESSAGE_WITH_DATE -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.constraintlayout_my_message_with_date, parent, false)
                MyMessageViewHolder(itemView = itemView)
            }
            MY_IMAGE_WITH_DATE -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.constraintlayout_my_image_with_date, parent, false)
                MyImageViewHolder(itemView = itemView)
            }
            OTHERS_MESSAGE_WITH_DATE -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.constraintlayout_others_message_with_date, parent, false)
                OthersMessageViewHolder(itemView = itemView)
            }
            MY_MESSAGE_WITHOUT_DATE -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.constraintlayout_my_message_without_date, parent, false)
                MyMessageWithoutDateViewHolder(itemView = itemView)
            }
            MY_IMAGE_WITHOUT_DATE -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.constraintlayout_my_image_without_date, parent, false)
                MyImageWithoutDateViewHolder(itemView = itemView)
            }
            OTHERS_MESSAGE_WITHOUT_DATE -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.constraintlayout_others_message_without_date, parent, false)
                OthersMessageWithoutDateViewHolder(itemView = itemView)
            }
            OTHERS_IMAGE_WITHOUT_DATE -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.constraintlayout_others_image_without_date, parent, false)
                OthersImageWithoutDateViewHolder(itemView = itemView)
            }
            else -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.constraintlayout_others_image_with_date, parent, false)
                OthersImageViewHolder(itemView = itemView)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (messageDomainList.isEmpty()) return

        when (holder.javaClass) {
            MyMessageViewHolder::class.java -> (holder as MyMessageViewHolder).bind(messageDomainList[position])
            MyMessageWithoutDateViewHolder::class.java -> (holder as MyMessageWithoutDateViewHolder).bind(messageDomainList[position])
            MyImageViewHolder::class.java -> (holder as MyImageViewHolder).bind(messageDomainList[position], imageList[position])
            MyImageWithoutDateViewHolder::class.java -> (holder as MyImageWithoutDateViewHolder).bind(messageDomainList[position], imageList[position])
            OthersMessageViewHolder::class.java -> (holder as OthersMessageViewHolder).bind(messageDomainList[position])
            OthersMessageWithoutDateViewHolder::class.java -> (holder as OthersMessageWithoutDateViewHolder).bind(messageDomainList[position])
            OthersImageViewHolder::class.java -> (holder as OthersImageViewHolder).bind(messageDomainList[position], imageList[position])
            OthersImageWithoutDateViewHolder::class.java -> (holder as OthersImageWithoutDateViewHolder).bind(messageDomainList[position], imageList[position])
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
        if (messageDomainList.isEmpty()) return EMPTY_MESSAGE_WITH_DATE

        val preMessage = if (position == 0) null else messageDomainList[position-1]
        val currentMessage = messageDomainList[position]

        return when (senderUID) {
            currentMessage.senderId -> {
                if (currentMessage.messageType == MessageDomain.TYPE_IMAGE) {
                    if (preMessage == null || convertSimpleDateFormatToTime(preMessage.sentTime)[0] != convertSimpleDateFormatToTime(currentMessage.sentTime)[0]) MY_IMAGE_WITH_DATE
                    else MY_IMAGE_WITHOUT_DATE
                }
                else {
                    if (preMessage == null || convertSimpleDateFormatToTime(preMessage.sentTime)[0] != convertSimpleDateFormatToTime(currentMessage.sentTime)[0]) MY_MESSAGE_WITH_DATE
                    else MY_MESSAGE_WITHOUT_DATE
                }
            }
            else -> {
                if (currentMessage.messageType == MessageDomain.TYPE_IMAGE) {
                    if (preMessage == null || convertSimpleDateFormatToTime(preMessage.sentTime)[0] != convertSimpleDateFormatToTime(currentMessage.sentTime)[0]) OTHERS_IMAGE_WITH_DATE
                    else OTHERS_IMAGE_WITHOUT_DATE
                }
                else {
                    if (preMessage == null || convertSimpleDateFormatToTime(preMessage.sentTime)[0] != convertSimpleDateFormatToTime(currentMessage.sentTime)[0]) OTHERS_MESSAGE_WITH_DATE
                    else OTHERS_MESSAGE_WITHOUT_DATE
                }
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

    fun setReaderLog(list: List<ChatroomDomain.ReaderLogDomain>) {
        this.readersLog.clear()
        this.readersLog.addAll(list)
        notifyDataSetChanged()
    }


}