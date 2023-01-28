package com.example.chatappsample.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.chatappsample.R
import com.example.chatappsample.domain.`interface`.EmailVerifyListener
import com.example.chatappsample.domain.dto.UserDomain
import com.example.chatappsample.domain.repository.UserRepository
import com.example.chatappsample.presentation.viewmodel.UserViewModel
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EmailVerificationFragment : Fragment() {
    @Inject
    lateinit var userRepo: UserRepository

    lateinit var fragmentVerification: View
    private val btnContinueWithVerifiedEmailButton by lazy { fragmentVerification.findViewById<MaterialButton>(R.id.btn_continue_if_email_verified) }
    private val viewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        fragmentVerification = inflater.inflate(R.layout.fragment_email_verification, container, false)
        return fragmentVerification
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnContinueWithVerifiedEmailButton.setOnClickListener(btnClickListener)
    }

    private val btnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            if (v == null) return

            if (v.id == R.id.btn_continue_if_email_verified) {
                viewModel.signUp("", onEmailVerifiedListener)
            }
        }
    }

    private val onEmailVerifiedListener = object: EmailVerifyListener {
        override fun onSuccess(user: UserDomain) {
            Toast.makeText(this@EmailVerificationFragment.requireActivity(), "인증이 완료됐습니다.", Toast.LENGTH_SHORT).show()
            this@EmailVerificationFragment.requireActivity()
                .supportFragmentManager
                .beginTransaction()
                .replace(R.id.framelayout_signin_activity, NameEditFragment())
                .commit()
        }

        override fun onFailEmailVerification() {
            Toast.makeText(this@EmailVerificationFragment.requireActivity(), "인증 메일 전송을 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
        }

        override fun onFail(exception: Exception) {
            exception.printStackTrace()
            Toast.makeText(this@EmailVerificationFragment.requireActivity(), "인증이 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
        }
    }
}