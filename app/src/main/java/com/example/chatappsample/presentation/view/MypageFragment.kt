package com.example.chatappsample.presentation.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.chatappsample.R
import com.example.chatappsample.databinding.FragmentMyPageBinding
import com.example.chatappsample.domain.dto.Message
import com.example.chatappsample.domain.dto.User
import com.example.chatappsample.presentation.viewmodel.UserViewModel
import com.example.chatappsample.util.CharLengthInputFilter
import com.example.chatappsample.util.LetterDigitsInputFilter
import com.google.android.material.imageview.ShapeableImageView
import java.text.SimpleDateFormat
import java.util.*

class MypageFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }
    private lateinit var mBinding: FragmentMyPageBinding
    private var onGetUserProfileListener: OnGetUserProfileListener? = null
    private var myProfile: User? = User()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_page, container, false)
        mBinding.lifecycleOwner = this.viewLifecycleOwner
        mBinding.viewModel = viewModel

        viewModel.currentUser.observe(this.viewLifecycleOwner) {
            if (it != null && myProfile?.profileImage != it.profileImage) {
                Glide
                    .with(requireContext())
                    .load(it.profileImage)
                    .centerCrop()
                    .into(mBinding.ivMyPageUserProfileImage)
            }

            myProfile = it
        }
        viewModel.isProfileEditMode.observe(this.viewLifecycleOwner) {
            mBinding.tvMyPageUserProfileNameRules.visibility = if (it) View.VISIBLE else View.INVISIBLE
        }

        mBinding.btnMyPageUserProfileEditSave.setOnClickListener {
            viewModel.toggleProfileEditMode(false)
            mBinding.root.requestFocus()
            viewModel.updateCurrentUser(myProfile!!.apply {
                name = mBinding.etMyPageUserProfileNameModify.text.toString()
            }, false)
        }

        mBinding.ivMyPageUserProfileEdit.setOnClickListener {
            viewModel.toggleProfileEditMode(true)
            mBinding.etMyPageUserProfileNameModify.let {
                it.requestFocus()
                it.setText(myProfile!!.name)
                it.filters = arrayOf(LetterDigitsInputFilter(), CharLengthInputFilter(10))
            }
        }

        val getProfileImageFromActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val selectedProfileImageUri = it?.data?.data!!

                myProfile?.let { profile ->
                    profile.profileImage = selectedProfileImageUri.toString()
                }

                if (myProfile != null) viewModel.updateCurrentUser(myProfile!!, true)
            }
        }

        mBinding.ivMyPageUserProfileImage.setOnClickListener {

            val mIntent = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_PICK
            }

            getProfileImageFromActivity.launch(mIntent)
        }

        mBinding.btnMyPageLogout.setOnClickListener {
            viewModel.cancelAutoLogin()
            viewModel.signOut()
            startActivity(Intent(requireActivity(), LogInActivity::class.java))
            requireActivity().finish()
        }

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