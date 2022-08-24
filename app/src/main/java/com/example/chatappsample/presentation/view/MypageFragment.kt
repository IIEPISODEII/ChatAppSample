package com.example.chatappsample.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.chatappsample.R
import com.example.chatappsample.databinding.FragmentMyPageBinding
import com.example.chatappsample.domain.dto.User
import com.example.chatappsample.presentation.viewmodel.UserViewModel
import com.google.android.material.imageview.ShapeableImageView

class MypageFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }
    private lateinit var mBinding: FragmentMyPageBinding
    private var onGetUserProfileListener: OnGetUserProfileListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_page, container, false)
        mBinding.lifecycleOwner = this.viewLifecycleOwner
        mBinding.viewModel = viewModel

        viewModel.currentUser.observe(this.viewLifecycleOwner) {}
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onGetUserProfileListener?.setOnGetUserProfileListener(mBinding.ivMyPageUserProfileImage)
    }

    interface OnGetUserProfileListener {
        fun setOnGetUserProfileListener(imageView: ShapeableImageView)
    }

    fun setOnGetUserProfile(listener: OnGetUserProfileListener) {
        this.onGetUserProfileListener = listener
    }
}