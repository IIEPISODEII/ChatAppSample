package com.example.chatappsample.presentation.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.chatappsample.R
import com.example.chatappsample.domain.`interface`.OnEmailVerificationListener
import com.example.chatappsample.domain.repository.UserRepository
import com.example.chatappsample.presentation.viewmodel.UserViewModel
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EmailVerificationFragment : Fragment() {
    @Inject
    lateinit var userRepo: UserRepository

    lateinit var mainFragment: View
    private val btnContinueWithVerifiedEmailButton by lazy { mainFragment.findViewById<MaterialButton>(R.id.btn_continue_if_email_verified) }
    private val viewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mainFragment = inflater.inflate(R.layout.fragment_email_verification, container, true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnContinueWithVerifiedEmailButton.setOnClickListener(btnClickListener)
    }

    private val btnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            if (v == null) return

            if (v.id == R.id.btn_continue_if_email_verified) {
                viewModel.sendVerificationEmail()
            }
        }
    }

    private val onEmailVerifiedListener = object: OnEmailVerificationListener {
        override fun onSuccess() {
            Toast.makeText(this@EmailVerificationFragment.requireActivity(), "인증 메일을 전송했습니다. 가입하신 이메일을 확인해주세요.", Toast.LENGTH_SHORT).show()
        }

        override fun onFailEmailVerification() {
            Toast.makeText(this@EmailVerificationFragment.requireActivity(), "인증 메일 전송을 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
        }

        override fun onFail(exception: Exception) {
            Toast.makeText(this@EmailVerificationFragment.requireActivity(), "인증이 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
        }
    }
}