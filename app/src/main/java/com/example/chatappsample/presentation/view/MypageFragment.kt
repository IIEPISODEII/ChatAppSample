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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.chatappsample.R
import com.example.chatappsample.databinding.FragmentMyPageBinding
import com.example.chatappsample.domain.`interface`.FileDownloadListener
import com.example.chatappsample.domain.dto.UserDomain
import com.example.chatappsample.presentation.viewmodel.UserViewModel
import com.example.chatappsample.util.CharLengthInputFilter
import com.example.chatappsample.util.LetterDigitsInputFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.annotation.Nullable

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

        viewModel.currentUserInfo.observe(viewLifecycleOwner) {
            if (it.uid.isEmpty()) return@observe

            val fileDownloadListenerImpl = object: FileDownloadListener {
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
            }

            myProfile = it

            mBinding.tvMyPageUserProfileName.text = myProfile!!.name
            viewModel.downloadProfileImage(myProfile!!.uid, fileDownloadListenerImpl)
        }

        viewModel.isProfileEditMode.observe(this.viewLifecycleOwner) {
            mBinding.tvMyPageUserProfileNameRules.visibility = if (it) View.VISIBLE else View.INVISIBLE
            mBinding.tvMyPageUserProfileName.visibility = if (!it) View.VISIBLE else View.INVISIBLE
            mBinding.etMyPageUserProfileNameModify.visibility = if (it) View.VISIBLE else View.INVISIBLE
            mBinding.ivMyPageUserProfileEdit.visibility = if (!it) View.VISIBLE else View.INVISIBLE
            mBinding.btnMyPageUserProfileEditSave.visibility = if (it) View.VISIBLE else View.INVISIBLE
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
            startActivity(Intent(requireActivity(), SignInActivity::class.java))
            requireActivity().finish()
        }
    }
}