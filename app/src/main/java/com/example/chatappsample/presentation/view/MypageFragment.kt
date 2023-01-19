package com.example.chatappsample.presentation.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
import com.example.chatappsample.domain.dto.UserDomain
import com.example.chatappsample.presentation.viewmodel.UserViewModel
import com.example.chatappsample.util.CharLengthInputFilter
import com.example.chatappsample.util.LetterDigitsInputFilter
import com.google.android.material.imageview.ShapeableImageView

class MypageFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }
    private lateinit var mBinding: FragmentMyPageBinding
    private var myProfile: UserDomain? = UserDomain()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_page, container, false)
        mBinding.lifecycleOwner = this.viewLifecycleOwner
        mBinding.viewModel = viewModel

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.currentUserDomain.observe(this@MypageFragment.requireActivity()) {
            if (it == null) return@observe
            myProfile = it

            viewModel.downloadProfileImage(it.uid, object: OnFileDownloadListener {
                override fun onSuccess(byteArray: ByteArray) {
                    Glide
                        .with(this@MypageFragment.requireActivity())
                        .load(byteArray)
                        .centerCrop()
                        .into(mBinding.ivMyPageUserProfileImage)
                }

                override fun onFail(e: Exception) {
                    Glide
                        .with(this@MypageFragment.requireActivity())
                        .load(R.drawable.ic_baseline_person_24)
                        .centerCrop()
                        .into(mBinding.ivMyPageUserProfileImage)
                    Log.e("Download Profile", e.message ?: "")
                }
            })
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
            startActivity(Intent(requireActivity(), SignInActivity::class.java))
            requireActivity().finish()
        }
    }
}